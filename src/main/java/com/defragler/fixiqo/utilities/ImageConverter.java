package com.defragler.fixiqo.utilities;

import java.io.*;
import javafx.scene.image.*;

public final class ImageConverter {
    
    private ImageConverter() { }

    public static Image fromBytes(byte[] data) {
        if (data == null || data.length == 0) return null;
        return new Image(new ByteArrayInputStream(data));
    }
}
