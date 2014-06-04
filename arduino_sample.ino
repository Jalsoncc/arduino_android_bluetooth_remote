/*@author @noelyahan */
char INBYTE; // incomming byte

void setup() {
  Serial.begin(9600);
}

void loop() {
  
  while (!Serial.available());
  INBYTE = Serial.read();
  Serial.print(INBYTE);
  delay(50);
}
