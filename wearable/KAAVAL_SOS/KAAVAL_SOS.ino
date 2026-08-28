#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <esp_mac.h>

constexpr int SOS_BUTTON_PIN = 4;
constexpr unsigned long SOS_HOLD_MS = 3000;

// These UUIDs match android/app/.../service/KaavalBleManager.kt.
#define KAAVAL_SERVICE_UUID "f0e0d0c0-b0a0-4000-8000-000000000001"
#define SOS_CHARACTERISTIC_UUID "f0e0d0c0-b0a0-4000-8000-000000000002"

BLECharacteristic* sosCharacteristic;
bool phoneConnected = false;
bool buttonWasDown = false;
bool sosAlreadySent = false;
unsigned long pressStartedAt = 0;

class ConnectionCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer*) override {
    phoneConnected = true;
    Serial.println("Phone connected.");
  }

  void onDisconnect(BLEServer*) override {
    phoneConnected = false;
    BLEDevice::startAdvertising();
    Serial.println("Phone disconnected; advertising again.");
  }
};

void sendSos() {
  if (!phoneConnected) {
    Serial.println("SOS detected, but no phone is connected.");
    return;
  }

  sosCharacteristic->setValue("SOS");
  sosCharacteristic->notify();
  Serial.println("SOS notification sent.");
}

void setup() {
  Serial.begin(115200);
  pinMode(SOS_BUTTON_PIN, INPUT_PULLUP);
  uint8_t kaavalBleBaseMac[] = { 0x02, 0x4B, 0x41, 0x41, 0x56, 0x01 };
  esp_base_mac_addr_set(kaavalBleBaseMac);


  BLEDevice::init("KAAVAL");
  BLEServer* server = BLEDevice::createServer();
  server->setCallbacks(new ConnectionCallbacks());

  BLEService* service = server->createService(KAAVAL_SERVICE_UUID);
  sosCharacteristic = service->createCharacteristic(
    SOS_CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY
  );
  sosCharacteristic->addDescriptor(new BLE2902());
  sosCharacteristic->setValue("READY");
  service->start();

  BLEAdvertising* advertising = BLEDevice::getAdvertising();
  advertising->addServiceUUID(KAAVAL_SERVICE_UUID);
  advertising->setScanResponse(true);
  BLEDevice::startAdvertising();
  Serial.println("KAAVAL ready. Hold the GPIO 4 button for 3 seconds.");
}

void loop() {
  const bool buttonIsDown = digitalRead(SOS_BUTTON_PIN) == LOW;

  if (buttonIsDown && !buttonWasDown) {
    pressStartedAt = millis();
    sosAlreadySent = false;
  }

  if (buttonIsDown && !sosAlreadySent && millis() - pressStartedAt >= SOS_HOLD_MS) {
    sendSos();
    sosAlreadySent = true;
  }

  buttonWasDown = buttonIsDown;
  delay(20);
}
