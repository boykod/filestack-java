package com.filestack.transforms;

import com.filestack.Config;
import com.filestack.FileLink;
import com.filestack.StorageOptions;
import com.filestack.internal.BaseService;
import com.filestack.internal.CdnService;
import com.filestack.internal.MockResponse;
import com.filestack.transforms.tasks.AvTransformOptions;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static com.filestack.UtilsKt.fileLink;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAvTransform {


  CdnService cdnService = mock(CdnService.class);
  BaseService baseService = mock(BaseService.class);

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorException() {
    Config config = new Config("apiKey");
    AvTransform transform = new AvTransform(cdnService, config, "handle", null, null);
  }

  @Test
  public void testConstructorNoStoreOpts() {
    AvTransformOptions avOpts = new AvTransformOptions.Builder()
        .preset("mp4")
        .build();

    Config config = new Config("apiKey");
    TransformTask task = new AvTransform(cdnService, config, "handle", null, avOpts).tasks.get(0);

    Assert.assertEquals("video_convert=preset:mp4", task.toString());
  }

  @Test
  public void testConstructorStoreOpts() {
    StorageOptions storeOpts = new StorageOptions.Builder()
        .container("some-bucket")
        .build();

    AvTransformOptions avOpts = new AvTransformOptions.Builder()
        .preset("mp4")
        .build();

    Config config = new Config("apiKey");
    TransformTask task = new AvTransform(cdnService, config, "handle", storeOpts, avOpts).tasks.get(0);

    Assert.assertEquals("video_convert=container:some-bucket,preset:mp4", task.toString());
  }

  @Test(expected = IOException.class)
  public void testGetFilelinkFail() throws Exception {

    ResponseBody body = ResponseBody.create(
        MediaType.get("application/json"),
        "{'status':'failed'}"
    );

    when(cdnService.transform("video_convert=preset:mp4", "handle"))
        .thenReturn(MockResponse.<ResponseBody>success(body));

    Config config = new Config("apiKey");
    FileLink fileLink = fileLink(config, cdnService, baseService, "handle");

    AvTransformOptions avOptions = new AvTransformOptions.Builder().preset("mp4").build();

    fileLink.avTransform(avOptions).getFileLink();
  }
}
