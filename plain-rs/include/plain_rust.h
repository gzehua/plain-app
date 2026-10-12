#ifndef PLAIN_RUST_H
#define PLAIN_RUST_H
#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>
typedef struct { uint8_t *data; size_t len; int32_t status; } PlainBleBuffer;
uint64_t plain_ble_new(void);
uint64_t plain_ble_encoder(uint32_t id, bool response, uint32_t limit, const uint8_t *data, size_t len);
void plain_ble_free(uint64_t handle);
uint64_t plain_ble_info(uint64_t handle);
PlainBleBuffer plain_ble_call(int32_t action, uint64_t handle, uint32_t id, uint32_t sequence, uint32_t limit, bool response, const uint8_t *data, size_t len);
void plain_ble_buffer_free(PlainBleBuffer buffer);

char *plain_prefs_open(const char *system_path, const char *user_path);
char *plain_prefs_user_snapshot(void);
char *plain_prefs_update_user(const char *patch_json);
char *plain_prefs_user_action(const char *action_json);
void plain_prefs_string_free(char *pointer);

char *plain_discovery_action(const char *facts_json, const char *request_json);
char *plain_peer_status_action(const char *command_json);

char *plain_core_start(const char *database_path, const char *token, const char *config_json);
void plain_core_stop(void);
char *plain_http_stop(void);
char *plain_web_login_decide(const char *request_id, bool allowed);
char *plain_dlna_receiver(const char *command);
char *plain_graphql(const char *request, bool host_api);
char *plain_events_next(const char *stream);
char *plain_events_send(const char *action, const char *message, const uint8_t *bytes, size_t len);

#endif
