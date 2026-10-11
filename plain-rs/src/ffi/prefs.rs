use crate::ffi::values::c_string;
use crate::ffi::values::input;
use plain_rs::prefs::Prefs;
use std::ffi::{CString, c_char};
use std::path::Path;
use std::sync::{Arc, Mutex};

static PREFS: Mutex<Option<Arc<Prefs>>> = Mutex::new(None);

pub(crate) fn prefs() -> Result<Arc<Prefs>, String> {
    PREFS
        .lock()
        .map_err(|e| e.to_string())?
        .clone()
        .ok_or_else(|| "Rust preferences are not initialized".to_string())
}

fn open(system_path: &str, user_path: &str) -> Result<(), String> {
    let mut guard = PREFS.lock().map_err(|e| e.to_string())?;
    if let Some(existing) = guard.as_ref() {
        if existing.path() == Path::new(system_path) && existing.user_path() == Path::new(user_path)
        {
            return Ok(());
        }
        return Err("Rust preferences were initialized with another path".to_string());
    }
    let loaded = Prefs::load_pair(Path::new(system_path), Path::new(user_path))
        .map_err(|e| e.to_string())?;
    *guard = Some(Arc::new(loaded));
    Ok(())
}

#[unsafe(no_mangle)]
pub extern "C" fn plain_prefs_open(
    system_path: *const c_char,
    user_path: *const c_char,
) -> *mut c_char {
    match input(system_path).and_then(|system_path| {
        input(user_path).and_then(|user_path| open(&system_path, &user_path))
    }) {
        Ok(()) => std::ptr::null_mut(),
        Err(error) => c_string(error),
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn plain_prefs_user_action(action_json: *const c_char) -> *mut c_char {
    match input(action_json).and_then(|action| run_user_action(&action)) {
        Ok(_) => std::ptr::null_mut(),
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

/// Applies one user-settings patch supplied as `{"field":value}`.
///
/// Delegates to `ContentServer::update_user_prefs`, which runs the same
/// `apply_user_patch` the GraphQL resolvers use: validation, persistence, the
/// `PREFS_UPDATED` broadcast remote clients rely on, and the mDNS /
/// desktop-access host effects. Requires the core to be started.
fn update_user(patch_json: &str) -> Result<bool, String> {
    let core = crate::ffi::server::CORE
        .lock()
        .map_err(|e| e.to_string())?;
    let core = core
        .as_ref()
        .ok_or("Rust core is not initialized")?;
    core.runtime
        .block_on(core.server.update_user_prefs(patch_json))
}

/// Runs one user-settings action — the commands that are more than a field
/// write, such as de-duplicating a recent search.
fn run_user_action(action_json: &str) -> Result<bool, String> {
    let core = crate::ffi::server::CORE
        .lock()
        .map_err(|e| e.to_string())?;
    let core = core
        .as_ref()
        .ok_or("Rust core is not initialized")?;
    core.runtime
        .block_on(core.server.run_user_action(action_json))
}

#[unsafe(no_mangle)]
pub extern "C" fn plain_prefs_user_snapshot() -> *mut c_char {
    match user_snapshot() {
        Ok(json) => c_string(json),
        Err(error) => c_string(format!("ERROR:{error}")),
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn plain_prefs_update_user(patch_json: *const c_char) -> *mut c_char {
    match input(patch_json).and_then(|patch| update_user(&patch)) {
        Ok(_) => std::ptr::null_mut(),
        Err(error) => c_string(error),
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
        system_path: JString<'_>,
        user_path: JString<'_>,
    ) {
        env.with_env(|_| -> BridgeResult<()> {
            open(&system_path.to_string(), &user_path.to_string()).map_err(BridgeError::Prefs)?;
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

    #[unsafe(no_mangle)]
    pub extern "system" fn Java_com_ismartcoding_plain_rust_bridge_RustPrefsBridge_updateUserNative(
        mut env: EnvUnowned<'_>,
        _: JObject<'_>,
        patch_json: JString<'_>,
    ) {
        env.with_env(|_| -> BridgeResult<()> {
            update_user(&patch_json.to_string()).map_err(BridgeError::Prefs)?;
            Ok(())
        })
        .resolve::<ThrowRuntimeExAndDefault>();
    }

    #[unsafe(no_mangle)]
    pub extern "system" fn Java_com_ismartcoding_plain_rust_bridge_RustPrefsBridge_userActionNative(
        mut env: EnvUnowned<'_>,
        _: JObject<'_>,
        action_json: JString<'_>,
    ) {
        env.with_env(|_| -> BridgeResult<()> {
            run_user_action(&action_json.to_string()).map_err(BridgeError::Prefs)?;
            Ok(())
        })
        .resolve::<ThrowRuntimeExAndDefault>();
    }
}
