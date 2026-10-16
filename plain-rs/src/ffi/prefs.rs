use crate::ffi::values::c_string;
use crate::ffi::values::input;
use plain_rs::prefs::Prefs;
use std::ffi::{CString, c_char};
use std::path::Path;
use std::sync::{Arc, Mutex};

static PREFS: Mutex<Option<Arc<Prefs>>> = Mutex::new(None);
static DIRECTORY: Mutex<Option<Arc<plain_server::assembly::data_directory::DataDirectory>>> =
    Mutex::new(None);

pub(crate) fn prefs() -> Result<Arc<Prefs>, String> {
    PREFS
        .lock()
        .map_err(|e| e.to_string())?
        .clone()
        .ok_or_else(|| "Rust preferences are not initialized".to_string())
}

fn open(data_dir: &str, cache_dir: &str) -> Result<(), String> {
    let directory = Path::new(data_dir);
    if !directory.is_absolute() {
        return Err("Absolute data directory required".into());
    }
    let paths = plain_rs::data_paths::DataPaths::new(directory);
    let system_path = paths.system_prefs();
    let user_path = paths.user_prefs();
    let mut guard = PREFS.lock().map_err(|e| e.to_string())?;
    if let Some(existing) = guard.as_ref() {
        if existing.path() == system_path.as_path() && existing.user_path() == user_path.as_path() {
            DIRECTORY
                .lock()
                .map_err(|error| error.to_string())?
                .as_ref()
                .ok_or("Rust data directory is not initialized")?
                .cache_paths()
                .root()
                .eq(Path::new(cache_dir))
                .then_some(())
                .ok_or("Rust cache directory cannot change after initialization")?;
            return Ok(());
        }
        return Err("Rust preferences were initialized with another path".to_string());
    }
    let data_directory = plain_server::assembly::data_directory::DataDirectory::open(
        directory,
        Path::new(cache_dir),
    )?;
    let loaded =
        Prefs::load_pair(system_path.as_path(), user_path.as_path()).map_err(|e| e.to_string())?;
    *DIRECTORY.lock().map_err(|error| error.to_string())? = Some(data_directory);
    *guard = Some(Arc::new(loaded));
    Ok(())
}

#[unsafe(no_mangle)]
pub extern "C" fn plain_prefs_open(
    data_dir: *const c_char,
    cache_dir: *const c_char,
) -> *mut c_char {
    match input(data_dir)
        .and_then(|directory| input(cache_dir).and_then(|cache| open(&directory, &cache)))
    {
        Ok(()) => std::ptr::null_mut(),
        Err(error) => c_string(error),
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn plain_prefs_string_free(pointer: *mut c_char) {
    if !pointer.is_null() {
        // SAFETY: pointer was returned by c_string and is freed exactly once.
        drop(unsafe { CString::from_raw(pointer) });
    }
}

/// The whole user-settings store as JSON, Rust defaults already merged.
///
/// One call seeds every App-side settings flow, so startup does not need a
/// per-key round trip and the Kotlin defaults never become a second source of
/// truth. Keys are the serde camelCase field names — the same names the
/// GraphQL schema uses.
fn user_snapshot() -> Result<String, String> {
    let prefs = prefs()?;
    let settings = plain_rs::prefs::user::snapshot(&prefs).map_err(|e| e.to_string())?;
    serde_json::to_string(&settings).map_err(|e| e.to_string())
}

#[unsafe(no_mangle)]
pub extern "C" fn plain_prefs_user_snapshot() -> *mut c_char {
    match user_snapshot() {
        Ok(json) => c_string(json),
        Err(error) => c_string(format!("ERROR:{error}")),
    }
}

#[cfg(target_os = "android")]
mod android {
    use super::*;
    use jni::EnvUnowned;
    use jni::errors::ThrowRuntimeExAndDefault;
    use jni::objects::{JObject, JString};
    use jni::sys::jstring;

    #[derive(Debug)]
    enum BridgeError {
        Jni(jni::errors::Error),
        Prefs(String),
    }

    impl std::fmt::Display for BridgeError {
        fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
            match self {
                Self::Jni(error) => write!(f, "{error}"),
                Self::Prefs(error) => write!(f, "{error}"),
            }
        }
    }

    impl std::error::Error for BridgeError {}

    impl From<jni::errors::Error> for BridgeError {
        fn from(error: jni::errors::Error) -> Self {
            Self::Jni(error)
        }
    }

    type BridgeResult<T> = std::result::Result<T, BridgeError>;

    #[unsafe(no_mangle)]
    pub extern "system" fn Java_com_ismartcoding_plain_rust_bridge_RustPrefsBridge_openNative(
        mut env: EnvUnowned<'_>,
        _: JObject<'_>,
        data_dir: JString<'_>,
        cache_dir: JString<'_>,
    ) {
        env.with_env(|_| -> BridgeResult<()> {
            open(&data_dir.to_string(), &cache_dir.to_string()).map_err(BridgeError::Prefs)?;
            Ok(())
        })
        .resolve::<ThrowRuntimeExAndDefault>();
    }

    #[unsafe(no_mangle)]
    pub extern "system" fn Java_com_ismartcoding_plain_rust_bridge_RustPrefsBridge_userSnapshotNative(
        mut env: EnvUnowned<'_>,
        _: JObject<'_>,
    ) -> jstring {
        env.with_env(|env| -> BridgeResult<_> {
            let json = user_snapshot().map_err(BridgeError::Prefs)?;
            Ok(env.new_string(json)?)
        })
        .resolve::<ThrowRuntimeExAndDefault>()
        .into_raw()
    }
}
