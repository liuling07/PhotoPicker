# PhotoPicker
An android library to pick photo from gallery

# Sample

###### multi-selection mode
![image](https://raw.githubusercontent.com/liuling07/PhotoPicker/master/photo-picker-sample03.png)
<br/>
###### single-selection mode
![image](https://raw.githubusercontent.com/liuling07/PhotoPicker/master/photo-picker-sample01.png)

![image](https://raw.githubusercontent.com/liuling07/PhotoPicker/master/photo-picker-sample02.png)
<br/>

###### gif
![image](https://raw.githubusercontent.com/liuling07/PhotoPicker/master/sample.gif)
<br/>
# Usage
```
Intent intent = new Intent(MainActivity.this, PhotoPickerActivity.class);
intent.putExtra(PhotoPickerActivity.EXTRA_SHOW_CAMERA, showCamera);
intent.putExtra(PhotoPickerActivity.EXTRA_SELECT_MODE, selectedMode);
intent.putExtra(PhotoPickerActivity.EXTRA_MAX_MUN, maxNum);
startActivityForResult(intent, PICK_PHOTO);
```

```
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(requestCode == PICK_PHOTO){
        if(resultCode == RESULT_OK){
            ArrayList<String> result = data.getStringArrayListExtra(PhotoPickerActivity.KEY_RESULT);
            //do what you want to to.
        }
    }
}
```
# About me
* Blog:[http://www.liuling123.com](http://www.liuling123.com)
* Email:[lauren.liuling@gmail.com](mailto:lauren.liuling@gmail.com)、[lauren_liuling@163.com](mailto:lauren_liuling@163.com)

# License
Copyright 2015 liuling

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
