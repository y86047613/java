package core.util;

import com.google.gson.Gson;
import com.jeefw.app.bean.BaseRequestBean;
import java.io.PrintStream;
import org.apache.log4j.Logger;

public class AppSendUtils
{
  private static final Logger Log = Logger.getLogger(AppSendUtils.class);
  
  /* Error */
  public static String connectURL(String dest_url, String commString)
  {
	return commString;
    // Byte code:
    //   0: ldc 27
    //   2: astore_2
    //   3: aconst_null
    //   4: astore_3
    //   5: aconst_null
    //   6: astore 4
    //   8: aconst_null
    //   9: astore 5
    //   11: aconst_null
    //   12: astore 6
    //   14: new 29	java/net/URL
    //   17: dup
    //   18: aload_0
    //   19: invokespecial 31	java/net/URL:<init>	(Ljava/lang/String;)V
    //   22: astore_3
    //   23: aload_3
    //   24: invokevirtual 34	java/net/URL:openConnection	()Ljava/net/URLConnection;
    //   27: checkcast 38	java/net/HttpURLConnection
    //   30: astore 4
    //   32: aload 4
    //   34: sipush 30000
    //   37: invokevirtual 40	java/net/HttpURLConnection:setReadTimeout	(I)V
    //   40: aload 4
    //   42: ldc 44
    //   44: invokevirtual 46	java/net/HttpURLConnection:setRequestMethod	(Ljava/lang/String;)V
    //   47: aload 4
    //   49: iconst_1
    //   50: invokevirtual 49	java/net/HttpURLConnection:setDoInput	(Z)V
    //   53: aload 4
    //   55: iconst_1
    //   56: invokevirtual 53	java/net/HttpURLConnection:setDoOutput	(Z)V
    //   59: aload 4
    //   61: invokevirtual 56	java/net/HttpURLConnection:getOutputStream	()Ljava/io/OutputStream;
    //   64: astore 5
    //   66: aload 5
    //   68: aload_1
    //   69: ldc 60
    //   71: invokevirtual 62	java/lang/String:getBytes	(Ljava/lang/String;)[B
    //   74: invokevirtual 68	java/io/OutputStream:write	([B)V
    //   77: aload 5
    //   79: invokevirtual 74	java/io/OutputStream:flush	()V
    //   82: aload 5
    //   84: invokevirtual 77	java/io/OutputStream:close	()V
    //   87: new 80	java/io/BufferedReader
    //   90: dup
    //   91: new 82	java/io/InputStreamReader
    //   94: dup
    //   95: aload 4
    //   97: invokevirtual 84	java/net/HttpURLConnection:getInputStream	()Ljava/io/InputStream;
    //   100: invokespecial 88	java/io/InputStreamReader:<init>	(Ljava/io/InputStream;)V
    //   103: invokespecial 91	java/io/BufferedReader:<init>	(Ljava/io/Reader;)V
    //   106: astore 6
    //   108: new 94	java/lang/StringBuffer
    //   111: dup
    //   112: invokespecial 96	java/lang/StringBuffer:<init>	()V
    //   115: astore 7
    //   117: goto +12 -> 129
    //   120: aload 7
    //   122: iload 8
    //   124: i2c
    //   125: invokevirtual 97	java/lang/StringBuffer:append	(C)Ljava/lang/StringBuffer;
    //   128: pop
    //   129: aload 6
    //   131: invokevirtual 101	java/io/BufferedReader:read	()I
    //   134: dup
    //   135: istore 8
    //   137: iconst_m1
    //   138: if_icmpgt -18 -> 120
    //   141: aload 7
    //   143: invokevirtual 105	java/lang/StringBuffer:toString	()Ljava/lang/String;
    //   146: astore_2
    //   147: goto +113 -> 260
    //   150: astore 7
    //   152: getstatic 16	core/util/AppSendUtils:Log	Lorg/apache/log4j/Logger;
    //   155: aload 7
    //   157: aload 7
    //   159: invokevirtual 109	org/apache/log4j/Logger:error	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   162: aload 5
    //   164: ifnull +8 -> 172
    //   167: aload 5
    //   169: invokevirtual 77	java/io/OutputStream:close	()V
    //   172: aload 4
    //   174: ifnull +8 -> 182
    //   177: aload 4
    //   179: invokevirtual 113	java/net/HttpURLConnection:disconnect	()V
    //   182: aload 6
    //   184: ifnull +23 -> 207
    //   187: aload 6
    //   189: invokevirtual 116	java/io/BufferedReader:close	()V
    //   192: goto +15 -> 207
    //   195: astore 10
    //   197: getstatic 16	core/util/AppSendUtils:Log	Lorg/apache/log4j/Logger;
    //   200: aload 10
    //   202: aload 10
    //   204: invokevirtual 109	org/apache/log4j/Logger:error	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   207: ldc 27
    //   209: areturn
    //   210: astore 9
    //   212: aload 5
    //   214: ifnull +8 -> 222
    //   217: aload 5
    //   219: invokevirtual 77	java/io/OutputStream:close	()V
    //   222: aload 4
    //   224: ifnull +8 -> 232
    //   227: aload 4
    //   229: invokevirtual 113	java/net/HttpURLConnection:disconnect	()V
    //   232: aload 6
    //   234: ifnull +23 -> 257
    //   237: aload 6
    //   239: invokevirtual 116	java/io/BufferedReader:close	()V
    //   242: goto +15 -> 257
    //   245: astore 10
    //   247: getstatic 16	core/util/AppSendUtils:Log	Lorg/apache/log4j/Logger;
    //   250: aload 10
    //   252: aload 10
    //   254: invokevirtual 109	org/apache/log4j/Logger:error	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   257: aload 9
    //   259: athrow
    //   260: aload 5
    //   262: ifnull +8 -> 270
    //   265: aload 5
    //   267: invokevirtual 77	java/io/OutputStream:close	()V
    //   270: aload 4
    //   272: ifnull +8 -> 280
    //   275: aload 4
    //   277: invokevirtual 113	java/net/HttpURLConnection:disconnect	()V
    //   280: aload 6
    //   282: ifnull +23 -> 305
    //   285: aload 6
    //   287: invokevirtual 116	java/io/BufferedReader:close	()V
    //   290: goto +15 -> 305
    //   293: astore 10
    //   295: getstatic 16	core/util/AppSendUtils:Log	Lorg/apache/log4j/Logger;
    //   298: aload 10
    //   300: aload 10
    //   302: invokevirtual 109	org/apache/log4j/Logger:error	(Ljava/lang/Object;Ljava/lang/Throwable;)V
    //   305: aload_2
    //   306: areturn
    // Line number table:
    //   Java source line #24	-> byte code offset #0
    //   Java source line #25	-> byte code offset #3
    //   Java source line #26	-> byte code offset #5
    //   Java source line #27	-> byte code offset #8
    //   Java source line #28	-> byte code offset #11
    //   Java source line #30	-> byte code offset #14
    //   Java source line #31	-> byte code offset #23
    //   Java source line #32	-> byte code offset #32
    //   Java source line #34	-> byte code offset #40
    //   Java source line #36	-> byte code offset #47
    //   Java source line #37	-> byte code offset #53
    //   Java source line #38	-> byte code offset #59
    //   Java source line #39	-> byte code offset #66
    //   Java source line #40	-> byte code offset #77
    //   Java source line #41	-> byte code offset #82
    //   Java source line #42	-> byte code offset #87
    //   Java source line #43	-> byte code offset #108
    //   Java source line #45	-> byte code offset #117
    //   Java source line #46	-> byte code offset #120
    //   Java source line #45	-> byte code offset #129
    //   Java source line #47	-> byte code offset #141
    //   Java source line #49	-> byte code offset #147
    //   Java source line #50	-> byte code offset #152
    //   Java source line #54	-> byte code offset #162
    //   Java source line #55	-> byte code offset #167
    //   Java source line #57	-> byte code offset #172
    //   Java source line #58	-> byte code offset #177
    //   Java source line #60	-> byte code offset #182
    //   Java source line #61	-> byte code offset #187
    //   Java source line #63	-> byte code offset #192
    //   Java source line #64	-> byte code offset #197
    //   Java source line #51	-> byte code offset #207
    //   Java source line #52	-> byte code offset #210
    //   Java source line #54	-> byte code offset #212
    //   Java source line #55	-> byte code offset #217
    //   Java source line #57	-> byte code offset #222
    //   Java source line #58	-> byte code offset #227
    //   Java source line #60	-> byte code offset #232
    //   Java source line #61	-> byte code offset #237
    //   Java source line #63	-> byte code offset #242
    //   Java source line #64	-> byte code offset #247
    //   Java source line #66	-> byte code offset #257
    //   Java source line #54	-> byte code offset #260
    //   Java source line #55	-> byte code offset #265
    //   Java source line #57	-> byte code offset #270
    //   Java source line #58	-> byte code offset #275
    //   Java source line #60	-> byte code offset #280
    //   Java source line #61	-> byte code offset #285
    //   Java source line #63	-> byte code offset #290
    //   Java source line #64	-> byte code offset #295
    //   Java source line #68	-> byte code offset #305
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	307	0	dest_url	String
    //   0	307	1	commString	String
    //   2	304	2	rec_string	String
    //   4	20	3	url	java.net.URL
    //   6	270	4	urlconn	java.net.HttpURLConnection
    //   9	257	5	out	java.io.OutputStream
    //   12	274	6	rd	java.io.BufferedReader
    //   115	27	7	sb	StringBuffer
    //   150	8	7	e	Exception
    //   120	3	8	ch	int
    //   135	3	8	ch	int
    //   210	48	9	localObject	Object
    //   195	8	10	e	Exception
    //   245	8	10	e	Exception
    //   293	8	10	e	Exception
    // Exception table:
    //   from	to	target	type
    //   14	147	150	java/lang/Exception
    //   162	192	195	java/lang/Exception
    //   14	162	210	finally
    //   212	242	245	java/lang/Exception
    //   260	290	293	java/lang/Exception
  }
  
  public static String SendToUrlByBean(BaseRequestBean brb)
  {
    String result = "";
    Gson gson = new Gson();
    String json = gson.toJson(brb);
    System.out.println(json);
    result = connectURL("http://localhost:8080/jeefw/client/face", json);
    return result;
  }
}
