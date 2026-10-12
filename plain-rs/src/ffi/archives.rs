use super::values::{c_string, input};
use plain_server::{native::archives::ArchiveEntry, services::files::archives::ArchiveReader};
use std::{
    ffi::c_char,
    sync::{
        Mutex,
        atomic::{AtomicU64, Ordering},
    },
    time::Duration,
};

const READ_TIMEOUT: Duration = Duration::from_secs(30);
const MAX_CHUNK: usize = 64 * 1024;

static ARCHIVES: Mutex<Vec<(u64, ArchiveReader)>> = Mutex::new(Vec::new());
static NEXT_HANDLE: AtomicU64 = AtomicU64::new(1);

pub(crate) fn clear() {
    if let Ok(mut archives) = ARCHIVES.lock() {
        archives.clear();
    }
}
fn put(handle: u64, reader: ArchiveReader) {
    if let Ok(mut archives) = ARCHIVES.lock() {
        archives.push((handle, reader));
    }
}
fn take(handle: u64) -> Result<ArchiveReader, String> {
    let mut archives = ARCHIVES.lock().map_err(|e| e.to_string())?;
    let index = archives
        .iter()
        .position(|(id, _)| *id == handle)
        .ok_or("Archive reader not open")?;
    Ok(archives.swap_remove(index).1)
}
fn open(entries: &str) -> Result<serde_json::Value, String> {
    let entries: Vec<ArchiveEntry> = serde_json::from_str(entries).map_err(|e| e.to_string())?;
    let (runtime, archives) = super::server::with_core(|core| {
        (core.runtime.handle().clone(), core.server.native_archives())
    })??;
    let reader = runtime.block_on(archives.files(entries))?;
    let size = reader.size;
    let handle = NEXT_HANDLE.fetch_add(1, Ordering::Relaxed);
    put(handle, reader);
    Ok(serde_json::json!({"handle":handle,"size":size}))
}
fn read(handle: u64, buffer: &mut [u8]) -> Result<usize, String> {
    let mut reader = take(handle)?;
    let runtime = super::server::with_core(|core| core.runtime.handle().clone())?;
    let outcome =
        runtime.block_on(async { tokio::time::timeout(READ_TIMEOUT, reader.read()).await });
    let chunk = match outcome {
        Err(_) => return Err("Archive read timed out".to_string()),
        Ok(Err(error)) => return Err(error),
        Ok(Ok(None)) => return Ok(0),
        Ok(Ok(Some(chunk))) => chunk,
    };
    put(handle, reader);
    let length = chunk.len().min(buffer.len());
    buffer[..length].copy_from_slice(&chunk[..length]);
    Ok(length)
}
fn envelope(result: Result<serde_json::Value, String>) -> String {
    match result {
        Ok(value) => serde_json::json!({"value":value}).to_string(),
        Err(error) => serde_json::json!({"error":error}).to_string(),
    }
}
#[unsafe(no_mangle)]
pub extern "C" fn plain_archive_open(entries: *const c_char) -> *mut c_char {
    c_string(envelope(input(entries).and_then(|entries| open(&entries))))
}
#[unsafe(no_mangle)]
pub unsafe extern "C" fn plain_archive_read(
    handle: u64,
    buffer: *mut u8,
    len: usize,
) -> *mut c_char {
    let result = if len < MAX_CHUNK {
        Err("Archive buffer must hold 64KiB".to_string())
    } else if buffer.is_null() {
        Err("Null archive buffer".to_string())
    } else {
        let buffer = unsafe { std::slice::from_raw_parts_mut(buffer, len) };
        read(handle, buffer).map(|bytes| serde_json::json!({"bytes":bytes,"eof":bytes == 0}))
    };
    c_string(envelope(result))
}
#[unsafe(no_mangle)]
pub extern "C" fn plain_archive_close(handle: u64) -> *mut c_char {
    c_string(take(handle).map(|_| ()).err().unwrap_or_default())
}
#[cfg(target_os = "android")]
mod android {
    use super::*;
    use jni::{
        EnvUnowned,
        errors::ThrowRuntimeExAndDefault,
        objects::{JObject, JString},
        sys::{jbyteArray, jlong, jstring},
    };
    #[unsafe(no_mangle)]
    pub extern "system" fn Java_com_ismartcoding_plain_rust_bridge_RustCoreBridge_archiveOpenNative(
        mut env: EnvUnowned<'_>,
        _: JObject<'_>,
        entries: JString<'_>,
    ) -> jstring {
        env.with_env(|env| -> jni::errors::Result<_> {
            env.new_string(envelope(open(&entries.to_string())))
        })
        .resolve::<ThrowRuntimeExAndDefault>()
        .into_raw()
    }
    #[unsafe(no_mangle)]
    pub extern "system" fn Java_com_ismartcoding_plain_rust_bridge_RustCoreBridge_archiveReadNative(
        mut env: EnvUnowned<'_>,
        _: JObject<'_>,
        handle: jlong,
        buffer: jbyteArray,
    ) -> jstring {
        env.with_env(|env| -> jni::errors::Result<_> {
            let result = if buffer.is_null() {
                Err("Null archive buffer".to_string())
            } else {
                let mut bytes = vec![0; MAX_CHUNK];
                match read(handle as u64, &mut bytes) {
                    Ok(length) => {
                        env.byte_array_from_slice(&bytes[..length])
                            .map_err(|error| error.to_string())?;
                        Ok(serde_json::json!({"bytes":length,"eof":length == 0}))
                    }
                    Err(error) => Err(error),
                }
            };
            env.new_string(envelope(result))
        })
        .resolve::<ThrowRuntimeExAndDefault>()
        .into_raw()
    }
    #[unsafe(no_mangle)]
    pub extern "system" fn Java_com_ismartcoding_plain_rust_bridge_RustCoreBridge_archiveCloseNative(
        mut env: EnvUnowned<'_>,
        _: JObject<'_>,
        handle: jlong,
    ) -> jstring {
        env.with_env(|env| -> jni::errors::Result<_> {
            env.new_string(take(handle as u64).map(|_| ()).err().unwrap_or_default())
        })
        .resolve::<ThrowRuntimeExAndDefault>()
        .into_raw()
    }
}
