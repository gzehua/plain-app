use super::*;

#[test]
fn server_ports_preserve_optional_zero_and_unsigned_boundaries() {
    assert_eq!(port(-1).unwrap(), None);
    assert_eq!(port(0).unwrap(), Some(0));
    assert_eq!(port(65535).unwrap(), Some(65535));
    for invalid in [-2, i32::MIN, 65536, i32::MAX] {
        assert!(port(invalid).is_err());
    }
}
