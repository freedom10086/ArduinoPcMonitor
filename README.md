# Arduino    
我的arduino应用,arduino驱动ds3231时钟芯片，DHT22温湿度传感器，并显示到OLED(128x64)上    

同时支持pwm控制rgb灯，支持控制颜色，颜色信息断电保存

支持蓝牙控制（cc2541模块 ble蓝牙）
* 支持蓝牙设置时间
* 支持蓝牙获取时间
* 支持蓝牙设置rgb灯
* 支持蓝牙获取rgb灯值

## 硬件连接
* DHT22 --- PIN 7
* LED_R --- PIN 6
* LED_G --- PIN 9
* LED_B --- PIN 5
* OLED_CS --- PIN 10
* OLED_DC ---PIN 8
* OLED_RST --- PIN 2
* OLED 数据 11 (MOSI),  13 (SCK)
* DS3221 I2C: A4 (SDA) and A5 (SCL)
* 蓝牙模块 --- 串口
