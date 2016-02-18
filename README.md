# opencv-image-diff

## Compile

```
$ mkdir bin
$ javac -cp /usr/local/share/OpenCV/java/opencv-300.jar -d bin \
    src/org/nebur/opencv/java/OpenCVImageDiff.java
```

Change classpath `/usr/local/share/OpenCV/java/opencv-300.jar` to your opencv installation path.

Move resources files to `bin` folder:

```
$ cp -rf src/res bin
```

## Run

```
$ java -cp /usr/local/share/OpenCV/java/opencv-300.jar:bin/ \
    -Djava.library.path=/usr/local/share/OpenCV/java/ \
    org.nebur.opencv.java.OpenCVImageDiff
```

Change library path `/usr/local/share/OpenCV/java/` to your opencv installation path.
