package org.example;

import com.google.cloud.functions.CloudEventsFunction;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.cloudevents.CloudEvent;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfConverter implements CloudEventsFunction {

  private static final Gson gson = new Gson();

  @Override
  public void accept(CloudEvent event) throws Exception {
    logger.info("Event Type: " + event.getType());
    logger.info("Event Subject: " + event.getSubject());

    String data = new String(event.getData().toBytes(), StandardCharsets.UTF_8);
    final var storageData = gson.fromJson(data, JsonObject.class);

    logger.info("protoPayload: " + storageData.get("protoPayload").getAsJsonObject());

    final String resourceName = storageData.getAsJsonObject("protoPayload").get("resourceName").getAsString();

    Pattern pattern = Pattern.compile("buckets/([^/]+)/objects/(.+)");
    Matcher matcher = pattern.matcher(resourceName);
    if (matcher.find()) {
      String bucket = matcher.group(1);
      String object = matcher.group(2);

      logger.info("bucket: " + bucket);
      logger.info("Objeto: " + object);
      convert(bucket, object);

    }
  }

  private static final Logger logger = Logger.getLogger(PdfConverter.class.getName());

  private static void convert(String bucketName, String objectName) {
    try {
      final byte[] bytDoc = downloadFile(bucketName, objectName);

      ByteArrayInputStream docInput = new ByteArrayInputStream(bytDoc);

      ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream();
      WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(docInput);

      Docx4J.toPDF(wordMLPackage, pdfOutput);

      uploadFile(bucketName, objectName + ".pdf", pdfOutput.toByteArray());
      logger.info("PDF generado con docx4j");

    } catch (Docx4JException | IOException e) {
      e.printStackTrace();
      logger.severe("Error al convertir el documento: " + e.getMessage());
    }
  }


  private static byte[] downloadFile(String bucketName, String objectName) throws IOException {

    final Storage storage = StorageOptions.getDefaultInstance().getService();

    // Descargar blob de GCS
    Blob blob = storage.get(bucketName, objectName);
    return blob.getContent();

  }

  private static void uploadFile(String bucketName, String objectName, byte[] content) {

    final Storage storage = StorageOptions.getDefaultInstance().getService();

    storage.create(
        Blob.newBuilder(bucketName, objectName).build(),
        content
    );
    // Subir blob a GCS
    System.out.println("PDF subido a bucket: " + bucketName + " con nombre: " + objectName);
  }
}
