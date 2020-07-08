package br.com.tiker.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import br.com.tiker.model.Constants;
import br.com.tiker.scene.ContentFileParser;
import br.com.tiker.model.Sticker;
import br.com.tiker.model.StickerPack;
import br.com.tiker.scene.StickerPacksContainer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StickerPacksManager {

    public static StickerPacksContainer stickerPacksContainer = null;

    public static List<Sticker> saveStickerPackFilesLocally(String identifier, List<Uri> stickersUries, Context context) {
        String stickerPath = Constants.STICKERS_DIRECTORY_PATH + identifier;
        List<Sticker> stickerList = new ArrayList<>();
        File directory = new File(stickerPath);
        if (!directory.exists()) {
            directory.mkdir();
        }
        for (Uri uri : stickersUries) {
            Sticker sticker = new Sticker(FileUtils.generateRandomIdentifier() + ".webp", null);
            stickerList.add(sticker);
            saveStickerFilesLocally(sticker, uri, stickerPath, context);
        }
        return stickerList;
    }

    private static void saveStickerFilesLocally(Sticker sticker, Uri stickerUri, String stickerPath, Context context) {
        createStickerImageFile(stickerUri, Uri.parse(stickerPath + "/" + sticker.imageFileName), context, Bitmap.CompressFormat.WEBP);
    }

    public static List<StickerPack> getStickerPacks(Context context) {
        List<StickerPack> stickerPackList = new ArrayList<>();

        if (RequestPermissionsHelper.verifyPermissions(context)) {
            File contentFile = new File(Constants.STICKERS_DIRECTORY_PATH + "contents.json");
            try (InputStream contentsInputStream = new FileInputStream(contentFile)) {
                stickerPackList = ContentFileParser.parseStickerPacks(contentsInputStream);
            } catch (IOException | IllegalStateException e) {
                //throw new RuntimeException("contents.json" + " file has some issues: " + e.getMessage(), e);
                Log.i("Content provider: ", "contents.json" + " file has some issues: " + e.getMessage());
            }
        }
        return stickerPackList;
    }

    public static void saveStickerPacksToJson(StickerPacksContainer container) {
        String json = new Gson().toJson(container);
        try {
            File file = new File(Constants.STICKERS_DIRECTORY_PATH + "/contents.json");
            Writer output = new BufferedWriter(new FileWriter(file));
            output.write(json);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createStickerImageFile(Uri sourceUri, Uri destinyUri, Context context, Bitmap.CompressFormat format) {
        String destinationFilename = destinyUri.getPath();
        try {
            File file = new File(destinationFilename);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().getParentFile().mkdirs();
                }
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            Bitmap bitmap = ImageUtils.compressImageToBytes(sourceUri, 70, 512, 512, context, format);
            OutputStream stream = null;
            stream = new FileOutputStream(file, false);
            bitmap.compress(Bitmap.CompressFormat.WEBP, 70, stream);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createStickerPackTrayIconFile(Uri sourceUri, Uri destinyUri, Context context) {
        String destinationFilename = destinyUri.getPath();
        try {
            File file = new File(destinationFilename);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().getParentFile().mkdirs();
                }
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            Bitmap bitmap = ImageUtils.compressImageToBytes(sourceUri, 80, 96, 96, context, Bitmap.CompressFormat.PNG);
            OutputStream stream = null;
            stream = new FileOutputStream(file, false);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteStickerPack(int index) {
        StickerPack pack = stickerPacksContainer.removeStickerPack(index);
        FileUtils.deleteFolder(Constants.STICKERS_DIRECTORY_PATH + pack.identifier);
        saveStickerPacksToJson(stickerPacksContainer);
    }

    public static void deleteStickerPack(String name) {
        int index = stickerPacksContainer.getIntexByName(name);
        if (index < 1000) {
            deleteStickerPack(index);
        }
    }

}