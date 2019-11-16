from flask import Flask, jsonify
from logentries import LogentriesHandler
import os
import time
import RPi.GPIO as GPIO
import Adafruit_DHT
from gpiozero import LightSensor
import numpy as np
from datetime import datetime as dt

app=Flask(__name__)

@app.route('/svetlo')
def funkcija1():
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(27, GPIO.IN)
    state = GPIO.input(27)
    return str(state)

@app.route('/tempZemlja')
def funkcija5():
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(22, GPIO.IN)
    hum,temp=Adafruit_DHT.read_retry(Adafruit_DHT.DHT11,22)
    print(temp)
    return str(temp)

@app.route('/voda')
def funkcija4():
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(4, GPIO.IN)
    state = GPIO.input(4)
    return str(state)

@app.route('/temp')
def funkcija2():
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(17, GPIO.IN)
    hum,temp=Adafruit_DHT.read_retry(Adafruit_DHT.DHT11,17)
    print(temp)
    return str(temp)

@app.route('/data')
def get_data():
    print('1')
    GPIO.setmode(GPIO.BCM)
    print('2')
    GPIO.setup(27, GPIO.IN)
    print('3')
    GPIO.setup(22, GPIO.IN)
    print('4')
    state = GPIO.input(27)
    print('5')
    hum_zemlja, temp_zemlja=Adafruit_DHT.read_retry(Adafruit_DHT.DHT11,22)
    print('6')
    GPIO.setup(4, GPIO.IN)
    print('7')
    voda = GPIO.input(4)
    print('8')
    GPIO.setmode(GPIO.BCM)
    print('9')
    GPIO.setup(17, GPIO.IN)
    print('10')
    hum,temp=Adafruit_DHT.read_retry(Adafruit_DHT.DHT11,17)
    
    data = {}
    print('11')
    data['air_temp'] =temp
    print('12')
    data['air_hum'] =hum
    print('13')
    data['light'] = state
    print('14')
    data['earth_temp'] = temp_zemlja
    print('15')
    data['earth_hum'] = hum_zemlja
    return jsonify(data)

@app.route('/hum')
def funkcij3():
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(17, GPIO.IN)
    hum,temp=Adafruit_DHT.read_retry(Adafruit_DHT.DHT11,17)
    return str(hum)


if __name__=='__main__':
    app.run(debug=True,host='0.0.0.0')