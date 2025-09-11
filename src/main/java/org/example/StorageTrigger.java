package org.example;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.gson.Gson;

public class StorageTrigger implements BackgroundFunction<StorageTrigger.GCSEvent> {

  private static final Gson gson = new Gson();

  @Override
  public void accept(GCSEvent event, Context context) {
    System.out.println("Bucket: " + event.bucket);
    System.out.println("File: " + event.name);
    Downloader.convert(event.bucket, event.name);
  }

  public static class GCSEvent {
    String bucket;
    String name;
    String contentType;
    String timeCreated;
    String updated;
  }
}
