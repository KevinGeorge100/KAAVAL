const fs=require('fs');const p='wearable/KAAVAL_SOS/KAAVAL_SOS.ino';let s=fs.readFileSync(p,'utf8');s=s.replace('#include <esp_mac.h>','#include <atomic>');s=s.replace('bool phoneConnected = false;','std::atomic<bool> phoneConnected{false};\nstd::atomic<int> requestedHaptic{0};\nunsigned long hapticStartedAt = 0;\nint activeHaptic = 0;');const a=s.indexOf('void pulseHaptic('),b=s.indexOf('class ConnectionCallbacks',a);s=s.slice(0,a)+`// BLE callbacks only queue feedback; GPIO timing belongs to the Arduino loop.
void pulseHaptic(int durationMs) { requestedHaptic.store(durationMs); }
void pulseReassurance() { requestedHaptic.store(-1); }
void updateHaptic() {
  const int next = requestedHaptic.exchange(0);
  if (next != 0) { activeHaptic = next; hapticStartedAt = millis(); }
  const unsigned long elapsed = millis() - hapticStartedAt;
  bool on = false;
  if (activeHaptic == -1) {
    on = elapsed < 120 || (elapsed >= 210 && elapsed < 390);
    if (elapsed >= 390) activeHaptic = 0;
  } else if (activeHaptic > 0) {
    on = elapsed < static_cast<unsigned long>(activeHaptic);
    if (!on) activeHaptic = 0;
  }
  digitalWrite(VIBRATION_MOTOR_PIN, on ? HIGH : LOW);
}

`+s.slice(b);
s=s.replace(/  \/\/ Set distinct MAC address[\s\S]*?esp_base_mac_addr_set\(kaavalBleBaseMac\);/,'  // Preserve the factory unique MAC; a shared fixed MAC breaks multi-device pairing.');
s=s.replace('  const bool buttonIsDown = (digitalRead(SOS_BUTTON_PIN) == LOW);',`  updateHaptic();
  static bool rawWasDown = false;
  static bool stableDown = false;
  static unsigned long changedAt = 0;
  const bool rawDown = digitalRead(SOS_BUTTON_PIN) == LOW;
  if (rawDown != rawWasDown) { rawWasDown = rawDown; changedAt = millis(); }
  if (millis() - changedAt >= 30) stableDown = rawDown;
  const bool buttonIsDown = stableDown;`);
s=s.replace('  delay(20);','  delay(1);');fs.writeFileSync(p,s);
