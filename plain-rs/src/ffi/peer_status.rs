use super::server::CORE;
use std::ffi::CStr;
use std::ffi::c_char;
use std::ffi::CString;

fn c_string(value: String) -> *mut c_char {
    CString::new(value).expect("string contains NUL").into_raw()
}

fn input(pointer: *const c_char) -> Result<String, String> {
    if pointer.is_null() {
        return Err("null string passed to peer-status FFI".to_string());
    }
    // SAFETY: callers pass a null-terminated string valid for this call.
    Ok(unsafe { CStr::from_ptr(pointer) }
        .to_str()
        .map_err(|e| e.to_string())?
        .to_owned())
}

#[unsafe(no_mangle)]
pub extern "C" fn plain_peer_status_action(command_json: *const c_char) -> *mut c_char {
    c_string(match input(command_json).and_then(|cmd| {
        let mut guard = CORE.lock().map_err(|e| e.to_string())?;
        let core = guard.as_mut().ok_or_else(|| "Rust core is not initialized".to_string())?;
        let controller = core.server.peer_status_controller();
        match core.runtime.block_on(controller.execute_json(&cmd)) {
            Ok(snapshot) => Ok(snapshot),
            Err(error) => Ok(format!("ERROR:{error}")),
        }
    }) {
        Ok(value) => value,
        Err(error) => format!("ERROR:{error}"),
    })
}