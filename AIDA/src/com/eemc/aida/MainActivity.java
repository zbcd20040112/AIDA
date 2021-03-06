package com.eemc.aida;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.ViewGroup.*;
import android.widget.*;
import com.gc.materialdesign.views.*;
import android.widget.ScrollView;
import com.gc.materialdesign.widgets.Dialog;
import java.io.*;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity
{
	RelativeLayout mainlayout;
	LinearLayout plist;
	JSONObject projects;
	Activity self=this;
	int width,height;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		WindowManager wm =(WindowManager)getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
		height = wm.getDefaultDisplay().getHeight();
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			height-=getResources().getDimensionPixelSize(resourceId);
		}
		
		try
		{
			initFiles();
		}
		catch (Exception e)
		{
			Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
		}

		mainlayout=new RelativeLayout(this);
        setContentView(mainlayout);
		RelativeLayout toolbar=new RelativeLayout(this);
		toolbar.setBackgroundColor(Color.parseColor("#1E88E5"));
		toolbar.setGravity(Gravity.CENTER|Gravity.LEFT);
		mainlayout.addView(toolbar,width,height/10);
		TextView title=new TextView(this);
		title.setTextSize(height/30);
		title.setX(20);
		title.setText("工程");
		title.setTextColor(Color.WHITE);
		toolbar.addView(title);
		
		ScrollView plv=new ScrollView(this);
		plv.setY(height/10);
		mainlayout.addView(plv,width,height-height/10);
		plist=new LinearLayout(this);
		plist.setOrientation(1);
		plv.addView(plist);
		try
		{
			for (int i=0;i<projects.getInt("num");i++)
			{
				addProjectButton(projects.getString(i+""));
			}
		}
		catch (Exception e)
		{
			
		}

		
		final ButtonFloat newpj=new ButtonFloat(this);
		newpj.setText("+");
		newpj.setRippleSpeed(30);
		newpj.setTextColor(Color.WHITE);
		newpj.setX(width-height/10-10);
		newpj.setY(height-height/10-10);
		newpj.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					final FileChooser fc=new FileChooser(self,"/sdcard");
					fc.setOnFiniEve(new Runnable(){
							@Override
							public void run()
							{
								try
								{
									for (int i=0;i<projects.getInt("num");i++)
									{
										if(projects.getString(i+"").equals(fc.chose.getPath())){
											Dialog d=new Dialog(self,"错误","你已添加过了");
											d.show();
											return;
										}
									}
									FileInputStream fis=new FileInputStream(fc.chose);
									byte b[]=new byte[4];
									fis.read(b);
									fis.close();
									if(b[0]==0x7f&&b[1]==0x45&&b[2]==0x4c&&b[3]==0x46){
									addProjectButton(fc.chose.getPath());
									projects.put("num",plist.getChildCount());
									projects.put(""+(plist.getChildCount()-1),fc.chose.getPath());
									}else{
										Dialog d=new Dialog(self,"错误","该文件不是有效的elf文件");
										d.show();
									}
								}
								catch (Exception e)
								{
								}
							}
						});
					fc.start();
				}
			});
		mainlayout.addView(newpj,height/10,height/10);
    }
	
	void addProjectButton(final String path){
		final ButtonRectangle pj=new ButtonRectangle(self);
		pj.setText(path.substring(path.lastIndexOf("/")+1));
		pj.setRippleSpeed(30);
		pj.setBackgroundColor(Color.WHITE);
		pj.setTextColor(Color.BLACK);
		pj.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					Intent intent=new Intent(self,AIDAActivity.class);
					intent.putExtra("path",path);
					startActivity(intent);
				}
			});
		plist.addView(pj,width,height/6);
	}
	
	void initFiles() throws Exception{
		File m=new File(Utils.mainPath);
		if(!m.exists()){
			m.mkdir();
		}
		File pjs=new File(Utils.mainPath+"/projects");
		if(!pjs.exists()){
			pjs.createNewFile();
			Utils.saveFile(Utils.mainPath+"/projects","{\"num\":0}".getBytes());
		}
			projects=new JSONObject(new String(Utils.readFile(Utils.mainPath+"/projects")));
			copyBin();
	}
	
	void copyBin(){
		try
		{
			byte[]b=new byte[240844];
			InputStream in=getAssets().open("disassembler");
			in.read(b);
			in.close();
			OutputStream out=openFileOutput("disassembler",MODE_WORLD_WRITEABLE|MODE_WORLD_READABLE);
			out.write(b);
			out.close();
			Runtime.getRuntime().exec("chmod 777 /data/data/com.eemc.aida/files/disassembler");
		}
		catch (Exception e)
		{
			Toast.makeText(this,""+e,Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Utils.saveFile(Utils.mainPath+"/projects",projects.toString().getBytes());
	}
	
	static
	{
        System.loadLibrary("aida");
    }
}
