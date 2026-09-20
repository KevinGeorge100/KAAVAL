#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <esp_mac.h>

/**
 * KAAVAL Tactile Wearable Wristband Firmware (ESP32)
 * 
 * Features:
 * 1. 3-Second Hold Tactile SOS Trigger (GPIO 4 with internal pullup).
 * 2. Instant Bidirectional Closed-Loop Reassurance:
 *    Listens for "REASSURE" write command from Android/Caregiver and pulses
 *    the onboard haptic vibration motor (GPIO 5).
 * 3. Haptic confirmation on SOS activation so visually impaired users feel
 *    physical confirmation that the signal was dispatched.
 */

constexpr int SOS_BUTTON_PIN = 4;
constexpr int VIBRATION_MOTOR_PIN = 5; // Haptic ERM/LRA vibration driver pin
constexpr unsigned long SOS_HOLD_MS = 3000;

// These UUIDs match android/app/.../service/KaavalBleManager.kt
#define KAAVAL_SERVICE_UUID "f0e0d0c0-b0a0-4000-8000-000000000001"
#define SOS_CHARACTERISTIC_UUID "f0e0d0c0-b0a0-4000-8000-000000000002"

BLECharacteristic* sosCharacteristic;
bool phoneConnected = false;
bool buttonWasDown = false;
bool sosAlreadySent = false;
unsigned long pressStartedAt = 0;

void pulseHaptic(int durationMs) {
  digitalWrite(VIBRATION_MOTOR_PIN, HIGH);
  delay(durationMs);
  digitalWrite(VIBRATION_MOTOR_PIN, LOW);
}

void pulseReassurance() {
  // Soothing double-pulse heartbeat pattern
  pulseHaptic(120);
  delay(90);
  pulseHaptic(180);
}

class ConnectionCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer*) override {
    phoneConnected = true;
    Serial.println("KAAVAL: Smartphone connected via BLE.");
    // Short friendly buzz on connection
    pulseHaptic(100);
  }

  void onDisconnect(BLEServer*) override {
    phoneConnected = false;
    BLEDevice::startAdvertising();
    Serial.println("KAAVAL: Smartphone disconnected; advertising restarted.");
  }
};

class SosCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic* pCharacteristic) override {
    String value = pCharacteristic->getValue().c_str();
    Serial.print("KAAVAL Wearable received command: ");
    Serial.println(value);

    if (value == "REASSURE") {
      Serial.println("KAAVAL: Caregiver Reassurance signal received! Pulsing haptics.");
      pulseReassurance();
    }
  }
};

void sendSos() {
  if (!phoneConnected) {
    Serial.println("KAAVAL: SOS button held, but no phone is connected.");
    // Long warning buzz indicating disconnection
    pulseHaptic(600);
    return;
  }

  // Tactile confirmation buzz to confirm SOS dispatched
  pulseHaptic(300);

  sosCharacteristic->setValue("SOS");
  sosCharacteristic->notify();
  Serial.println("KAAVAL: SOS BLE notification transmitted to phone.");
}

void setup() {
  Serial.begin(115200);
  
  pinMode(SOS_BUTTON_PIN, INPUT_PULLUP);
  pinMode(VIBRATION_MOTOR_PIN, OUTPUT);
  digitalWrite(VIBRATION_MOTOR_PIN, LOW);

  // Set distinct MAC address for KAAVAL device identification
  uint8_t kaavalBleBaseMac[] = { 0x02, 0x4B, 0x41, 0x41, 0x56, 0x01 };
  esp_base_mac_addr_set(kaavalBleBaseMac);

  BLEDevice::init("KAAVAL");
  BLEServer* server = BLEDevice::createServer();
  server->setCallbacks(new ConnectionCallbacks());

  BLEService* service = server->createService(KAAVAL_SERVICE_UUID);
  sosCharacteristic = service->createCharacteristic(
    SOS_CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_READ | 
    BLECharacteristic::PROPERTY_NOTIFY | 
    BLECharacteristic::PROPERTY_WRITE
  );
  sosCharacteristic->addDescriptor(new BLE2902());
  sosCharacteristic->setCallbacks(new SosCharacteristicCallbacks());
  sosCharacteristic->setValue("READY");
  service->start();

  BLEAdvertising* advertising = BLEDevice::getAdvertising();
  advertising->addServiceUUID(KAAVAL_SERVICE_UUID);
  advertising->setScanResponse(true);
  advertising->setMinPreferred(0x06); // Functions that help with iPhone connections issue
  advertising->setMaxPreferred(0x12);
  BLEDevice::startAdvertising();

  Serial.println("KAAVAL Wearable Ready. Hold GPIO 4 button for 3 seconds.");
  
  // Power-on ready confirmation
  pulseHaptic(150);
}

void loop() {
  const bool buttonIsDown = (digitalRead(SOS_BUTTON_PIN) == LOW);

  if (buttonIsDown && !buttonWasDown) {
    pressStartedAt = millis();
    sosAlreadySent = false;
  }

  if (buttonIsDown && !sosAlreadySent && (millis() - pressStartedAt >= SOS_HOLD_MS)) {
    sendSos();
    sosAlreadySent = true;
  }

  buttonWasDown = buttonIsDown;
  delay(20);
}
