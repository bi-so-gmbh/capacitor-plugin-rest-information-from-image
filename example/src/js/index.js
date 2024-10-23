/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { Capacitor } from '@capacitor/core';
import { RestInformation } from '@biso_gmbh/capacitor-plugin-rest-information-from-image';
import data from '../data.json';

const options = {
  beepOnSuccess: false,
  vibrateOnSuccess: false,
  detectorSize: 0.9,
  detectorAspectRatio: '5:1',
  drawFocusRect: true,
  focusRectColor: '#FFFFFF',
  focusRectBorderRadius: 10,
  focusRectBorderThickness: 5,
  drawFocusLine: false,
  focusLineColor: '#ff2d37',
  focusLineThickness: 2,
  drawFocusBackground: false,
  focusBackgroundColor: '#66FFFFFF',
  loadingCircleColor: '#888888',
  loadingCircleSize: 30,
  imageWidth: 1280,
  imageHeight: 720,
  imageCompression: 0.75,
  saveImage: false,
  imageName: "debug",
  androidImageLocation: "Download/rest-information-test-app"
};

init();

function onSuccess(result) {
  const node = document.createElement('div');
  node.className = 'log_item';
  node.textContent = `${JSON.stringify(result, undefined, 2)}`;
  document.getElementById('output').prepend(node);
}

function onFail(result) {
  const node = document.createElement('div');
  node.className = 'log_item';
  if ("data" in result) {
    node.textContent = `${JSON.stringify(result.data, undefined, 2)}`;
  } else {
    node.textContent = `${result}`;
  }
  document.getElementById('output').prepend(node);
}

async function scan() {
  console.log('scan button clicked');
  for (const key in options) {
    const element = document.getElementById(key);
    if (element) {
      if (element.tagName === 'INPUT' && element.type === 'checkbox') {
        options[key] = element.checked;
      } else {
        options[key] = element.value;
      }
    }
  }

  try {
    let result = await RestInformation.scan({
      request: data,
      settings: options,
    });
    console.log('result', JSON.stringify(result, null, 2));
    onSuccess(result);
  } catch (error) {
    console.log("error", error)
    console.log("error json", JSON.stringify(error, null, 2));
    onFail(error);
  }
}

function clearLog() {
  let logItems = document.getElementsByClassName('log_item');
  logItems = [...logItems];
  for (const item of logItems) {
    item.parentNode.removeChild(item);
  }
}

function init() {
  console.log('Running capacitor-' + Capacitor.getPlatform());
  document.getElementById('scan').onclick = scan;
  document.getElementById('clearLog').onclick = clearLog;

  for (const key in options) {
    const element = document.getElementById(key);
    if (element) {
      if (element.tagName === 'INPUT' && element.type === 'range') {
        element.addEventListener('input', updateTextInput);
        element.nextElementSibling.value = options[key];
        element.value = options[key];
      } else if (element.tagName === 'INPUT' && element.type === 'checkbox') {
        element.checked = options[key];
      } else {
        element.value = options[key];
      }
    }
  }
}

function updateTextInput() {
  document.getElementById(this.id).nextElementSibling.value = this.value;
}
