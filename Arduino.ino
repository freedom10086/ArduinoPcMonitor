#include <Arduino.h>
#include <U8g2lib.h>
#include <Wire.h>
#include <SPI.h>
#include "DHT.h"

#define DHTPIN 7
#define DHTTYPE DHT22   // DHT 22  (AM2302), AM2321
#define DS3231_ADDRESS 0x68
#define EEPROM_ADDRESS 0x57
#define RGB_ADDRESS 0x00

#define LED_R_PIN  6
#define LED_G_PIN  9
#define LED_B_PIN  5

#define BLUTOOTH_PIN  A0

#define DecToBcd(val) ((uint8_t) ((val / 10 * 16) + (val % 10)))
#define BcdToDec(val) ((uint8_t) ((val / 16 * 10) + (val % 16)))

#define RES_OK  0xFF
#define RES_ERROR  0x55

float hum = 0;
float temp = 0;
uint8_t loopcount = 0;
int blutooch_state = 0;

uint8_t _red = 0;
uint8_t _green = 0;
uint8_t _blue = 0;


U8G2_SSD1306_128X64_NONAME_1_4W_HW_SPI u8g2(U8G2_R0, /* cs=*/ 10, /* dc=*/ 8,  /*reset=*/ 2);
DHT dht(DHTPIN, DHTTYPE);

void writeEEPROM(const unsigned int address, const uint8_t data);
void drawClock();
void(* reboot) (void) = 0; //制造重启命令

void setup(void) {
  dht.begin();
  u8g2.begin();
  Wire.begin();
  Serial.begin(9600);
  pinMode(LED_R_PIN, OUTPUT);
  pinMode(LED_G_PIN, OUTPUT);
  pinMode(LED_B_PIN, OUTPUT);
  
  loadRGB();
  drawBoot();
  initBlutooth();
}

void loop(void) {
  uint32_t lasttime = millis();
  paraseCmd();
  u8g2.firstPage();
  do {
    drawClock();
    drawDht();
  } while ( u8g2.nextPage() );
  
  loopcount++;
  if(loopcount>=200){
    loopcount = 0;
  }

  if(millis()-lasttime<1000){
    delay(1000-(millis()-lasttime));
  }
}

void initBlutooth(){
  delay(30);
  Serial.println("AT+NAMEyangPcTool");
  delay(120);
  while(Serial.read() >= 0){};
  Serial.println("AT+PIN666666");
  delay(120);
  while(Serial.read() >= 0){};
}

void drawBoot(){
  uint8_t j;
  for(byte i=0;i<9;i++){
    u8g2.firstPage();
  do {
    u8g2.setFont(u8g2_font_9x15_tr);
  u8g2.setCursor(28,23);
    u8g2.print(F("Starting"));
    u8g2.setCursor(50,30);
    j = (i+1)%3;
    u8g2.print('.');
    while(j--){
      u8g2.print('.');
    }
    u8g2.setFont(u8g2_font_8x13_tr);
    u8g2.setCursor(10,62);
    u8g2.print(F("V1.0 by yang"));
  } while ( u8g2.nextPage() );
  delay(500);
  }
}

void drawDht(){
  if(loopcount%10==0){
    hum = dht.readHumidity();
    temp = dht.readTemperature();
  }

  blutooch_state = analogRead(BLUTOOTH_PIN);
  
  u8g2.setFont(u8g2_font_8x13_tr);

  char   c[4];  
  dtostrf(temp,2,1,c);
  u8g2.setCursor(0, 62);
  u8g2.print("T:"+ String(c));

  if(blutooch_state>500){
    u8g2.setCursor(60, 62);
    u8g2.print('.');
  }

  dtostrf(hum,2,1,c);
  u8g2.setCursor(81, 62);
  u8g2.print("H:"+ String(c));
}

void drawClock(){
  Wire.beginTransmission(DS3231_ADDRESS);
  Wire.write(0); // set DS3231 register pointer to 00h
  Wire.endTransmission();
  Wire.requestFrom(DS3231_ADDRESS, 7);
  
  uint8_t _second = Wire.read();
  _second = BcdToDec(_second);

  uint8_t _minute = Wire.read();//分
  _minute = BcdToDec(_minute);
  uint8_t _hour = Wire.read() & 0b111111;
  _hour = BcdToDec(_hour);
  uint8_t _week = Wire.read();
  _week = BcdToDec(_week);
  uint8_t _day = Wire.read();
  _day = BcdToDec(_day);
  uint8_t _month = Wire.read();
  _month = BcdToDec(_month);
  uint8_t _year = Wire.read();
  _year = BcdToDec(_year);
  
  u8g2.setFont(u8g2_font_freedoomr25_tn);
  u8g2.setCursor(18 , 45);
  if(_hour<10){
    u8g2.print('0');
  }
  u8g2.print(_hour);
  u8g2.print(':');
  if(_minute<10){
    u8g2.print('0');
  }
  u8g2.print(_minute);
 
  u8g2.setFont(u8g2_font_freedoomr10_tu);
  u8g2.setCursor(108, 45);
  if(_second<10){
    u8g2.print('0');
  }
  u8g2.print(_second);

  u8g2.setFont(u8g2_font_8x13_tr);
  u8g2.setCursor(6, 11);
  u8g2.print(20);
  u8g2.print(_year);
  u8g2.print('-');
  u8g2.print(_month);
  u8g2.print('-');
  u8g2.print(_day);

  u8g2.setCursor(90, 11);
  u8g2.print(getWeekStr(_week));
}


void setRGB(int r,int g,int b){
      r =  constrain(r, 0, 255);
      g =  constrain(g, 0, 255);
      b =  constrain(b, 0, 255);
      
      analogWrite(LED_R_PIN, r);
      analogWrite(LED_G_PIN, g);
      analogWrite(LED_B_PIN, b);

      _red = r;
      _green = g;
      _blue = b;

      writeEEPROM(RGB_ADDRESS,r);
      writeEEPROM(RGB_ADDRESS+1,g);
      writeEEPROM(RGB_ADDRESS+2,b);

      Serial.print(RES_OK);
      Serial.print(' ');
      Serial.print(_red);
      Serial.print(' ');
      Serial.print(_green);
      Serial.print(' ');
      Serial.println(_blue);
}

void loadRGB(){
     _red = readEEPROM(RGB_ADDRESS);
     _green = readEEPROM(RGB_ADDRESS+1);
     _blue = readEEPROM(RGB_ADDRESS+2);

      analogWrite(LED_R_PIN, _red);
      analogWrite(LED_G_PIN, _green);
      analogWrite(LED_B_PIN, _blue);
}


void setTime(const uint8_t year,const uint8_t month,const uint8_t day,const uint8_t week,const uint8_t hour,const uint8_t minute,const uint8_t second){
    Wire.beginTransmission(DS3231_ADDRESS);
    Wire.write(0); // set next input to start at the seconds register
    Wire.write(DecToBcd(second)); // set seconds
    Wire.write(DecToBcd(minute)); // set minutes
    Wire.write(DecToBcd(hour)); // set hours
    Wire.write(DecToBcd(week)); // set day of week (1=Sunday, 7=Saturday)
    Wire.write(DecToBcd(day)); // set date (1 to 31)
    Wire.write(DecToBcd(month)); // set month
    Wire.write(DecToBcd(year)); // set year (0 to 99)
    Wire.endTransmission();

    Serial.print(RES_OK);
    Serial.print(' ');
    Serial.print(year);
    Serial.print(' ');
    Serial.print(month);
    Serial.print(' ');
    Serial.print(day);
    Serial.print(' ');
    Serial.print(week);
    Serial.print(' ');
    Serial.print(hour);
    Serial.print(' ');
    Serial.print(minute);
    Serial.print(' ');
    Serial.println(second);
}

void showTime(){
  Wire.beginTransmission(DS3231_ADDRESS);
  Wire.write(0); // set DS3231 register pointer to 00h
  Wire.endTransmission();
  Wire.requestFrom(DS3231_ADDRESS, 7);
  
  uint8_t _second = Wire.read();
  _second = BcdToDec(_second);
  uint8_t _minute = Wire.read();//分
  _minute = BcdToDec(_minute);
  uint8_t _hour = Wire.read() & 0b111111;
  _hour = BcdToDec(_hour);
  uint8_t _week = Wire.read();
  _week = BcdToDec(_week);
  uint8_t _day = Wire.read();
  _day = BcdToDec(_day);
  uint8_t _month = Wire.read();
  _month = BcdToDec(_month);
  uint8_t _year = Wire.read();
  _year = BcdToDec(_year);

  Serial.print(RES_OK);
  Serial.print(' ');
  Serial.print(_year);
  Serial.print(' ');
  Serial.print(_month);
  Serial.print(' ');
  Serial.print(_day);
  Serial.print(' ');
  Serial.print(_week);
  Serial.print(' ');
  Serial.print(_hour);
  Serial.print(' ');
  Serial.print(_minute);
  Serial.print(' ');
  Serial.println(_second);
}

//解析命令
//设置rgb 101 r g b \n
void paraseCmd(){
  while(Serial.available()){
     int cmd = Serial.parseInt();
     switch (cmd){
      case 100://设置时间
      int year,month,day,week,hour,minute,second;
      year = Serial.parseInt();
      month = Serial.parseInt();
      day = Serial.parseInt();
      week = Serial.parseInt();
      hour = Serial.parseInt();
      minute = Serial.parseInt();
      second = Serial.parseInt();
      if (Serial.read() == '\n'&&year<100&&year>0&&month>0&&month<13&&day>0&&day<32&&week>0&&week<8&&hour>=0&&hour<25&&minute>=0&&minute<61&&second>=0&&second<61) {
            setTime(year,month,day,week,hour,minute,second);
        }else{
          Serial.println(RES_ERROR);
        }
      break;
      case 101://显示时间
        if (Serial.read() == '\n'){
          showTime();
        }else{
          Serial.println(RES_ERROR);
        }
      break;
      case 102://设置rgb灯
        int r,g, b;
        r= Serial.parseInt();
        g = Serial.parseInt();
        b = Serial.parseInt();
        
        if (Serial.read() == '\n') {
            setRGB(r,g,b);
        }else{
          Serial.println(RES_ERROR);
        }
        break;
      case 103://显示rgb
        if (Serial.read() == '\n'){
            Serial.print(RES_OK);
            Serial.print(' ');
            Serial.print(_red);
            Serial.print(' ');
            Serial.print(_green);
            Serial.print(' ');
            Serial.println(_blue);
        }else{
          Serial.println(RES_ERROR);
        }
      break;
      case 104://显示温湿度
        if (Serial.read() == '\n'){
            Serial.print(RES_OK);
            Serial.print(' ');
            Serial.print(temp);
            Serial.print(' ');
            Serial.println(hum);
        }else{
          Serial.println(RES_ERROR);
        }
      break;
      case 999://重启
        if (Serial.read() == '\n'){
             Serial.println(RES_OK);
             delay(200);
             reboot();//重启
        }else{
          Serial.println(RES_ERROR);
        }
        break;
        default:
          Serial.println(cmd);
        break;
    }
   }
}

uint8_t readEEPROM(const unsigned int address) {
    unsigned int rdata = 0xFF;
    Wire.beginTransmission(EEPROM_ADDRESS);
    Wire.write((int)(address >> 8)); // MSB
    Wire.write((int)(address & 0xFF)); // LSB
    Wire.endTransmission();
    Wire.requestFrom(EEPROM_ADDRESS, 1);
    if (Wire.available()) {
      rdata = Wire.read();
    }
    return rdata;
}

void writeEEPROM(const unsigned int address, const uint8_t data) {
    Wire.beginTransmission(EEPROM_ADDRESS);
    Wire.write((int)(address >> 8)); // MSB
    Wire.write((int)(address & 0xFF)); // LSB
    Wire.write(data);
    delay(5); // Little delay to assure EEPROM is able to process data; if missing and inside for look meses some values
    Wire.endTransmission();
}

String getWeekStr(uint8_t week){
  switch(week){
   case 1:
   return "Sun.";
   case 2:
   return "Mon.";
   case 3:
   return "Tues.";
   case 4:
   return "Wed.";
   case 5:
   return "Thur.";
   case 6:
   return "Fri.";
   case 7:
   return "Sat.";
   default:
    return "Err.";
  }
}




