package com.quran.labs.androidquran.data;

import com.quran.labs.androidquran.ui.util.TypefaceManager;

public class QuranFileConstants {
  // server urls
  public static final String BASE_HOST = "http://android.quran.com/data/";
  public static final String IMG_BASE_URL = BASE_HOST + "qaloon/";
  public static final String IMG_ZIP_BASE_URL = IMG_BASE_URL + "zips/";
  public static final String PATCH_ZIP_BASE_URL = IMG_BASE_URL + "patches/v";
  public static final String DATABASE_BASE_URL = BASE_HOST + "databases/";
  public static final String AYAHINFO_BASE_URL = IMG_BASE_URL + "databases/ayahinfo/";
  public static final String AUDIO_DB_BASE_URL = IMG_BASE_URL + "databases/audio/";
  public static final int FONT_TYPE = TypefaceManager.TYPE_UTHMANI_HAFS;

  // local paths
  public static final String QURAN_BASE = "quran_android/";
  public static final String DATABASE_DIRECTORY = "databases";
  public static final String AUDIO_DIRECTORY = "qaloon/audio";
  public static final String AYAHINFO_DIRECTORY = "qaloon/" + DATABASE_DIRECTORY;
  public static final String IMAGES_DIRECTORY = "qaloon";

  // arabic database
  public static final String ARABIC_DATABASE = "quran.ar.db";

  // images version
  public static final int IMAGES_VERSION = 2;
}
