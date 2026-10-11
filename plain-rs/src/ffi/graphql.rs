use super::values::{c_string, input};
use std::ffi::c_char;

fn execute(request: &str, host_api: bool) -> String {
    let result = (|| {
        let parsed = serde_json::from_str(request).map_err(|e| e.to_string())?;
        let (runtime, executor) = {
            let core = super::server::CORE.lock().map_err(|e| e.to_string())?;
            let core = core.as_ref().ok_or("Rust core not initialized")?;
            (
                core.runtime.handle().clone(),
                core.server.graphql_executor(),
            )
        };
        serde_json::to_string(&runtime.block_on(executor.execute(parsed, host_api)))
            .map_err(|e| e.to_string())
    })();
    result.unwrap_or_else(|error: String| {
        serde_json::json!({"errors":[{"message":error}]}).to_string()
    })
}
#[unsafe(no_mangle)]
pub extern "C" fn plain_graphql(request: *const c_char, host_api: bool) -> *mut c_char {
    c_string(match input(request) {
        Ok(request) => execute(&request, host_api),
        Err(error) => serde_json::json!({"errors":[{"message":error}]}).to_string(),
    })
}
#[cfg(target_os = "android")]
mod android {
    use super::*;
    use jni::{
        EnvUnowned,
        errors::ThrowRuntimeExAndDefault,
        objects::{JObject, JString},
        sys::{jboolean, jstring},
    };
    #[unsafe(no_mangle)]
    pub extern "system" fn Java_com_ismartcoding_plain_rust_bridge_RustCoreBridge_graphqlNative(
        mut env: EnvUnowned<'_>,
        _: JObject<'_>,
        request: JString<'_>,
        host_api: jboolean,
    ) -> jstring {
        env.with_env(|env| -> jni::errors::Result<_> {
            env.new_string(execute(&request.to_string(), host_api))
        })
        .resolve::<ThrowRuntimeExAndDefault>()
        .into_raw()
    }
}
