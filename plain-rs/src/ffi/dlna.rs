use super::values::{c_string, input};
use std::ffi::c_char;

fn execute(command: &str) -> Result<String, String> {
    let (runtime, controller) = {
        let core = super::server::CORE
            .lock()
            .map_err(|error| error.to_string())?;
        let core = core.as_ref().ok_or("Rust core not initialized")?;
        (core.runtime.handle().clone(), core.server.dlna_receiver())
    };
    runtime.block_on(controller.execute_json(command))
}

#[unsafe(no_mangle)]
pub extern "C" fn plain_dlna_receiver(command: *const c_char) -> *mut c_char {
    c_string(match input(command).and_then(|command| execute(&command)) {
        Ok(snapshot) => snapshot,
        Err(error) => format!("ERROR:{error}"),
    })
}

#[cfg(target_os = "android")]
mod android {
    use super::*;
    use jni::{
        EnvUnowned,
        errors::ThrowRuntimeExAndDefault,
        objects::{JObject, JString},
        sys::jstring,
    };

    #[unsafe(no_mangle)]
    pub extern "system" fn Java_com_ismartcoding_plain_rust_bridge_RustDlnaBridge_receiverNative(
        mut env: EnvUnowned<'_>,
        _: JObject<'_>,
        command: JString<'_>,
    ) -> jstring {
        env.with_env(|env| -> jni::errors::Result<_> {
            env.new_string(match execute(&command.to_string()) {
                Ok(snapshot) => snapshot,
                Err(error) => format!("ERROR:{error}"),
            })
        })
        .resolve::<ThrowRuntimeExAndDefault>()
        .into_raw()
    }
}
