# ArmorVox Java Exemplar

This project is open software designed as an exemplar and starting-point for partners and customers using Auraya's ArmorVox speaker recognition product.

This exemplar code show how a Java client connects to an ArmorVox server using API v8 (RESTful, JSON requests and responses). It is run as a command line application that specifies the API to call and the location of audio files and other parameters. It is intended to be the basis from which other developers can create their own client applications.

## Installing

To install and run this exemplar, you'll need to have:

* [Maven](https://maven.apache.org/)
* [Java 8](http://docs.oracle.com/javase/8/docs/)

To build this project:

```
mvn clean package
```

Once built, the executable jar file is in the target folder. 

To get a list of all the options, just run the executable with -h:

```
java -jar target/armorvox-client.jar -h
```

## API Call Usage

ArmorVox v8 endpoint is specified by the server **-s** option and the group (licence name) **-g** option.

 An utterance path is specified using the **-u** option. Multiple paths can be specified.
 An ID is specified with the **-i** option. Multiple IDs can be specified.
 A print name is specified withe **-pn** option.
 A phrase can be checked by using the **-p** option. Specify a phrase as 'file' indicates the actual phrase is in a file adjacent to the .wav audio file.

This table shows the mapping between API name and it's corresponding acronym specified with **-a** option:

| API Name         | Acronym |
| ---------------- |:-------:|
| enrol            | e       |
| verify           | v       |
| cross_match      | cm      |
| delete           | d       |
| get_voiceprint   | gvp     |
| check_quality    | cq      |
| check_similarity | cs      |
| detect_gender    | dg      |
| check_model_rank | cmr     |


#### Sample Call 1:

```
java -jar target/armorvox-client.jar -a e -g abcgroup -s https://cloud.armorvox.com/evaluation/v8 -i emma -u example_data/emma/emma-e-digit-1.wav  -u example_data/emma/emma-e-digit-2.wav  -u example_data/emma/emma-e-digit-3.wav
```

where

> 
* API **enrol** is called
* **abcgroup**  is a valid group name in the licence
* ArmorVox Server's URL is **https://cloud.armorvox.com/evaluation/v8**
* ID is enrolled as 'emma'
* 3 Audio utterances are enrolled
* Default print name is **digit**

#### Sample Call 2:

```
java -jar target/armorvox-client.jar -a e -g abcgroup -s https://cloud.armorvox.com/evaluation/v8 -pn digit -p file -i john -u example_data/john/john-e-digit-1.wav  -u example_data/john/john-e-digit-2.wav  -u example_data/john/john-e-digit-3.wav
```

where

> 
* API **enrol** is called
* **abcgroup**  is a valid group name in the licence
* ArmorVox server's URL is **https://cloud.armorvox.com/evaluation/v8**
* Print name is explicitly set to **digit**
* Phrase is checked: it assumes translation text files are in the same audio folder and given that same file name as per audio file but end with **.txt**
* ID is enrolled as 'john'
* 3 Audio utterances are enrolled



#### Sample Call 3:

```
java -jar target/armorvox-client.jar -a cm -g abcgroup -s https://cloud.armorvox.com/evaluation/v8 -i john -i emma -u example_data/john/john-v-digit-1.wav 
```

where

> 
* API **cross_match** is called
* **abcgroup**  is a valid group name in the licence
* ArmorVox Server's URL is **https://cloud.armorvox.com/evaluation/v8**
* IDs verified are 'john' and 'emma'
* 1 Audio utterance
* Default print name is **digit**




## License
Copyright 2017 Auraya Systems Pty Limited

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
