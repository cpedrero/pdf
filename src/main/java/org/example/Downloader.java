package org.example;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;


public class Downloader {
  private static final Logger logger = Logger.getLogger(Downloader.class.getName());

  public static void convert(String bucketName, String objectName) {
    try {
      final byte[] bytDoc = downloadFile(bucketName, objectName);

      ByteArrayInputStream docInput = new ByteArrayInputStream(bytDoc);

      ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream();
      WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(docInput);

      Docx4J.toPDF(wordMLPackage, pdfOutput);

      uploadFile(bucketName, objectName+".pdf", pdfOutput.toByteArray());
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

