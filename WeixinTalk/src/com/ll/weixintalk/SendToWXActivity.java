package com.ll.weixintalk;

import java.io.File;
import java.net.URL;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ll.weixintalk.uikit.CameraUtil;
import com.ll.weixintalk.uikit.MMAlert;
import com.ll.weixintalk.uikit.Util;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXEmojiObject;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXMusicObject;
import com.tencent.mm.sdk.openapi.WXTextObject;
import com.tencent.mm.sdk.openapi.WXVideoObject;
import com.tencent.mm.sdk.openapi.WXWebpageObject;

public class SendToWXActivity extends Activity {

	private static final int THUMB_SIZE = 150;

	private static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	private IWXAPI api;
	private static final int MMAlertSelect1  =  0;
	private static final int MMAlertSelect2  =  1;
	private static final int MMAlertSelect3  =  2;

	private CheckBox isTimelineCb;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
		
		setContentView(R.layout.send_to_wx);
		initView();
	}

	private void initView() {

		isTimelineCb = (CheckBox) findViewById(R.id.is_timeline_cb);
		isTimelineCb.setChecked(false);
		
		// send to weixin
		findViewById(R.id.send_text).setOnClickListener(new View.OnClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
								
				final EditText editor = new EditText(SendToWXActivity.this);
				editor.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				editor.setText(R.string.send_text_default);
								
				MMAlert.showAlert(SendToWXActivity.this, "send text", editor, getString(R.string.app_share), getString(R.string.app_cancel), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String text = editor.getText().toString();
						if (text == null || text.length() == 0) {
							return;
						}
						
						// 初始化一个WXTextObject对象
						WXTextObject textObj = new WXTextObject();
						textObj.text = text;

						// 用WXTextObject对象初始化一个WXMediaMessage对象
						WXMediaMessage msg = new WXMediaMessage();
						msg.mediaObject = textObj;
						// 发送文本类型的消息时，title字段不起作用
						// msg.title = "Will be ignored";
						msg.description = text;

						// 构造一个Req
						SendMessageToWX.Req req = new SendMessageToWX.Req();
						req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
						req.message = msg;
						req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
						
						// 调用api接口发送数据到微信
						api.sendReq(req);
						finish();
					}
				}, null);
			}
		});

		findViewById(R.id.send_img).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MMAlert.showAlert(SendToWXActivity.this, getString(R.string.send_img), 
						SendToWXActivity.this.getResources().getStringArray(R.array.send_img_item),
						null, new MMAlert.OnAlertSelectId(){

					@Override
					public void onClick(int whichButton) {						
						switch(whichButton){
						case MMAlertSelect1: {
							Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.send_img);
							WXImageObject imgObj = new WXImageObject(bmp);
							
							WXMediaMessage msg = new WXMediaMessage();
							msg.mediaObject = imgObj;
							
							Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
							bmp.recycle();
							msg.thumbData = Util.bmpToByteArray(thumbBmp, true);  // 设置缩略图

							SendMessageToWX.Req req = new SendMessageToWX.Req();
							req.transaction = buildTransaction("img");
							req.message = msg;
							req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
							api.sendReq(req);
							
							finish();
							break;
						}
						case MMAlertSelect2: {
							String path = SDCARD_ROOT + "/test.png";
							File file = new File(path);
							if (!file.exists()) {
								String tip = SendToWXActivity.this.getString(R.string.send_img_file_not_exist);
								Toast.makeText(SendToWXActivity.this, tip + " path = " + path, Toast.LENGTH_LONG).show();
								break;
							}
							
							WXImageObject imgObj = new WXImageObject();
							imgObj.setImagePath(path);
							
							WXMediaMessage msg = new WXMediaMessage();
							msg.mediaObject = imgObj;
							
							Bitmap bmp = BitmapFactory.decodeFile(path);
							Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
							bmp.recycle();
							msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
							
							SendMessageToWX.Req req = new SendMessageToWX.Req();
							req.transaction = buildTransaction("img");
							req.message = msg;
							req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
							api.sendReq(req);
							
							finish();
							break;
						}
						case MMAlertSelect3: {
							String url = "http://img.ui.cn/data/file/3/0/8/14803.png?imageView2/2/q/90";
							
							Log.i("TAG", "发送url图片");
							
							try{
								WXImageObject imgObj = new WXImageObject();
								imgObj.imageUrl = url;
								Log.i("TAG", "imgObj"+imgObj);
								
								WXMediaMessage msg = new WXMediaMessage();
								msg.mediaObject = imgObj;
								Log.i("TAG", "msg"+msg);

								Bitmap bmp = BitmapFactory.decodeStream(new URL(url).openStream());
//								Bitmap bmp = SendToWXActivity.this.getImage(url);
								Log.i("TAG", "bmp"+bmp);
								
								Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
								bmp.recycle();
								Log.i("TAG", "thumbBmp"+thumbBmp);
								
								msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
								
								SendMessageToWX.Req req = new SendMessageToWX.Req();
								req.transaction = buildTransaction("img");
								req.message = msg;
								
								Log.i("TAG", "msg"+msg);
								
								
								req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
								api.sendReq(req);
								
								finish();
							} catch(Exception e) {
								e.printStackTrace();
							}
					
							break;
						}
						default:
							break;
						}
					}
					
				});
			}
		});

		findViewById(R.id.send_music).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				MMAlert.showAlert(SendToWXActivity.this, getString(R.string.send_music),
						SendToWXActivity.this.getResources().getStringArray(R.array.send_music_item),
						null, new MMAlert.OnAlertSelectId(){

					@Override
					public void onClick(int whichButton) {						
						switch(whichButton){
						case MMAlertSelect1: {
							WXMusicObject music = new WXMusicObject();
							//music.musicUrl = "http://www.baidu.com";
							music.musicUrl="http://staff2.ustc.edu.cn/~wdw/softdown/index.asp/0042515_05.ANDY.mp3";
							//music.musicUrl="http://120.196.211.49/XlFNM14sois/AKVPrOJ9CBnIN556OrWEuGhZvlDF02p5zIXwrZqLUTti4o6MOJ4g7C6FPXmtlh6vPtgbKQ==/31353278.mp3";

							WXMediaMessage msg = new WXMediaMessage();
							msg.mediaObject = music;
							msg.title = "音乐标题";
							msg.description = "音乐简介";

							Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.send_music_thumb);
							msg.thumbData = Util.bmpToByteArray(thumb, true);

							SendMessageToWX.Req req = new SendMessageToWX.Req();
							req.transaction = buildTransaction("music");
							req.message = msg;
							req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
							api.sendReq(req);
							
							finish();
							break;
						}
						case MMAlertSelect2: {
							WXMusicObject music = new WXMusicObject();
							music.musicLowBandUrl = "http://www.qq.com";

							WXMediaMessage msg = new WXMediaMessage();
							msg.mediaObject = music;
							msg.title = "Music Title";
							msg.description = "Music Album";

							Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.send_music_thumb);
							msg.thumbData = Util.bmpToByteArray(thumb, true);

							SendMessageToWX.Req req = new SendMessageToWX.Req();
							req.transaction = buildTransaction("music");
							req.message = msg;
							req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
							api.sendReq(req);
							
							finish();
							break;
						}
						default:
							break;
						}
					}
				});
			}
		});
		
		findViewById(R.id.send_video).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MMAlert.showAlert(SendToWXActivity.this, getString(R.string.send_video), 
						SendToWXActivity.this.getResources().getStringArray(R.array.send_video_item),
						null, new MMAlert.OnAlertSelectId(){

					@Override
					public void onClick(int whichButton) {						
						switch(whichButton){
						case MMAlertSelect1: {
							WXVideoObject video = new WXVideoObject();
							video.videoUrl = "http://www.baidu.com";

							WXMediaMessage msg = new WXMediaMessage(video);
							msg.title = "视频标题";
							msg.description = "视频简介";
							Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.send_music_thumb);
							msg.thumbData = Util.bmpToByteArray(thumb, true);
							
							SendMessageToWX.Req req = new SendMessageToWX.Req();
							req.transaction = buildTransaction("video");
							req.message = msg;
							req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
							api.sendReq(req);
							
							finish();
							break;
						}
						case MMAlertSelect2: {
							WXVideoObject video = new WXVideoObject();
							video.videoLowBandUrl = "http://www.qq.com";

							WXMediaMessage msg = new WXMediaMessage(video);
							msg.title = "视频标题";
							msg.description = "视频简介";

							SendMessageToWX.Req req = new SendMessageToWX.Req();
							req.transaction = buildTransaction("video");
							req.message = msg;
							req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
							api.sendReq(req);
							
							finish();
							break;
						}
						default:
							break;
						}
					}
				});
			}
		});

		findViewById(R.id.send_webpage).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {				
				MMAlert.showAlert(SendToWXActivity.this, getString(R.string.send_webpage),
						SendToWXActivity.this.getResources().getStringArray(R.array.send_webpage_item),
						null, new MMAlert.OnAlertSelectId(){

					@Override
					public void onClick(int whichButton) {						
						switch(whichButton){
						case MMAlertSelect1:
							WXWebpageObject webpage = new WXWebpageObject();
							webpage.webpageUrl = "http://www.jyhelper.com";
							WXMediaMessage msg = new WXMediaMessage(webpage);
							msg.title = "网页标题";
							msg.description = "网页描述";
							Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.send_music_thumb);
							msg.thumbData = Util.bmpToByteArray(thumb, true);
							
							SendMessageToWX.Req req = new SendMessageToWX.Req();
							req.transaction = buildTransaction("webpage");
							req.message = msg;
							req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
							api.sendReq(req);
							
							finish();
							break;
						default:
							break;
						}
					}
				});
			}
		});

		findViewById(R.id.send_appdata).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MMAlert.showAlert(SendToWXActivity.this, getString(R.string.send_appdata), 
					SendToWXActivity.this.getResources().getStringArray(R.array.send_appdata_item),
					null, new MMAlert.OnAlertSelectId(){

					@Override
					public void onClick(int whichButton) {
						switch(whichButton){
						case MMAlertSelect1:
							final String dir = SDCARD_ROOT + "/tencent/";
							File file = new File(dir);
							if (!file.exists()) {
								file.mkdirs();
							}
							CameraUtil.takePhoto(SendToWXActivity.this, dir, "send_appdata", 0x101);
							break;
						case MMAlertSelect2: {
							final WXAppExtendObject appdata = new WXAppExtendObject();
							final String path = SDCARD_ROOT + "/test.png";
							appdata.fileData = Util.readFromFile(path, 0, -1);
							appdata.extInfo = "this is ext info";

							final WXMediaMessage msg = new WXMediaMessage();
							msg.setThumbImage(Util.extractThumbNail(path, 150, 150, true));
							msg.title = "标题";
							msg.description = "描述";
							msg.mediaObject = appdata;
							
							SendMessageToWX.Req req = new SendMessageToWX.Req();
							req.transaction = buildTransaction("appdata");
							req.message = msg;
							req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
							api.sendReq(req);
							
							finish();
							break;
						}
						case MMAlertSelect3: {
							// send appdata with no attachment
							final WXAppExtendObject appdata = new WXAppExtendObject();
							appdata.extInfo = "this is ext info";
							final WXMediaMessage msg = new WXMediaMessage();
							msg.title = "this is title";
							msg.description = "this is description";
							msg.mediaObject = appdata;
							
							SendMessageToWX.Req req = new SendMessageToWX.Req();
							req.transaction = buildTransaction("appdata");
							req.message = msg;
							req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
							api.sendReq(req);
							
							finish();
							break;
						}
						default:
							break;
						}
					}
					
				});
			}
		});
		
		findViewById(R.id.send_emoji).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {				
				MMAlert.showAlert(SendToWXActivity.this, getString(R.string.send_emoji),
						SendToWXActivity.this.getResources().getStringArray(R.array.send_emoji_item),
						null, new MMAlert.OnAlertSelectId(){

					@Override
					public void onClick(int whichButton) {						
						final String EMOJI_FILE_PATH = SDCARD_ROOT + "/emoji.gif";
						final String EMOJI_FILE_THUMB_PATH = SDCARD_ROOT + "/emojithumb.jpg";				
						switch(whichButton){
						case MMAlertSelect1: {
							WXEmojiObject emoji = new WXEmojiObject();
							emoji.emojiPath = EMOJI_FILE_PATH;
							
							WXMediaMessage msg = new WXMediaMessage(emoji);
							msg.title = "Emoji Title";
							msg.description = "Emoji Description";
							msg.thumbData = Util.readFromFile(EMOJI_FILE_THUMB_PATH, 0, (int) new File(EMOJI_FILE_THUMB_PATH).length());
				
							
							SendMessageToWX.Req req = new SendMessageToWX.Req();
							req.transaction = buildTransaction("emoji");
							req.message = msg;
							req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
							api.sendReq(req);
							
							finish();
							break;
						}
						
						case MMAlertSelect2: {
							WXEmojiObject emoji = new WXEmojiObject();
							emoji.emojiData = Util.readFromFile(EMOJI_FILE_PATH, 0, (int) new File(EMOJI_FILE_PATH).length());
							WXMediaMessage msg = new WXMediaMessage(emoji);
							
							msg.title = "Emoji Title";
							msg.description = "Emoji Description";
							msg.thumbData = Util.readFromFile(EMOJI_FILE_THUMB_PATH, 0, (int) new File(EMOJI_FILE_THUMB_PATH).length());
							
							SendMessageToWX.Req req = new SendMessageToWX.Req();
							req.transaction = buildTransaction("emoji");
							req.message = msg;
							req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
							api.sendReq(req);
							
							finish();
							break;
						}
						default:
							break;
						}
					}
				});
			}
		});

		// get token
		findViewById(R.id.get_token).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// send oauth request
				final SendAuth.Req req = new SendAuth.Req();
				req.scope = "post_timeline";
				req.state = "none";
				api.sendReq(req);
				finish();
			}
		});
		
		// unregister from weixin
		findViewById(R.id.unregister).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				api.unregisterApp();
			}
		});
	}
	
	/**
	 * 从网上下载图片
	 * @param path
	 * @return
	 * @throws Exception
	 */
//	public Bitmap getImage(String path) throws Exception {
//        URL url = new URL(path);
//        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
//        conn.setReadTimeout(10 * 1000);
//        conn.setConnectTimeout(10 * 1000);
//        conn.setRequestMethod("GET");
//        InputStream is = null;
//        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
//            is = conn.getInputStream();
//        } else {
//            is = null;
//        }
//        if (is == null){
//            throw new RuntimeException("stream is null");
//        } else {
//            try {
//                byte[] data=readStream(is);
//                if(data!=null){
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                    return bitmap;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            is.close();
//            return null;
//        }
//    }
//
///*
//     * 得到图片字节流 数组大小
//     * */
//    public static byte[] readStream(InputStream inStream) throws Exception{      
//        ByteArrayOutputStream outStream = new ByteArrayOutputStream();      
//        byte[] buffer = new byte[1024];      
//        int len = 0;      
//        while( (len=inStream.read(buffer)) != -1){      
//            outStream.write(buffer, 0, len);      
//        }      
//        outStream.close();      
//        inStream.close();      
//        return outStream.toByteArray();      
//    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {

		case 0x101: {
			final WXAppExtendObject appdata = new WXAppExtendObject();
			final String path = CameraUtil.getResultPhotoPath(this, data, SDCARD_ROOT + "/tencent/");
			appdata.filePath = path;
			appdata.extInfo = "this is ext info";

			final WXMediaMessage msg = new WXMediaMessage();
			msg.setThumbImage(Util.extractThumbNail(path, 150, 150, true));
			msg.title = "this is title";
			msg.description = "this is description";
			msg.mediaObject = appdata;
			
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = buildTransaction("appdata");
			req.message = msg;
			req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
			api.sendReq(req);
			
			finish();
			break;
		}
		default:
			break;
		}
	}

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
}
