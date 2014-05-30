package com.llb.souyou.util;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class Rotate3D extends Animation{
	private Camera camera;//不是相机那个Camera
	private float fromDegree;//开始旋转的角度
	private float endDegree;
	private float centerX;//旋转中心
	private float centerY;
	public Rotate3D(float fromDegree, float endDegree,float centerX, float centerY) {
		super();
		this.fromDegree = fromDegree;
		this.endDegree = endDegree;
		this.centerX = centerX;
		this.centerY = centerY;
	}
	
	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		// TODO Auto-generated method stub
		super.initialize(width, height, parentWidth, parentHeight);
		camera=new Camera();//每次动画前都会调用，不知道是放这里还是构造函数中，待验证
	}
	
	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);
		float rotateDegree=fromDegree+(endDegree-fromDegree)*interpolatedTime;//旋转角度
		Matrix matrix=t.getMatrix();//返回一个3*3矩阵
		
//		Log.i("Llb","rotate0"+rotateDegree);
		if (rotateDegree<-70) {
			rotateDegree=-90.0f;//若转个180°就完全反向了
			camera.save();//保存当前状态
			camera.rotateY(rotateDegree);//绕Y轴旋转
			camera.getMatrix(matrix);
			camera.restore();//恢复状态
//			Log.i("llb","rotate1"+rotateDegree);
		}else if (rotateDegree>70) {
			rotateDegree=90.0f;
			camera.save();//保存当前状态
			camera.rotateY(rotateDegree);//绕Y轴旋转
			camera.getMatrix(matrix);
			camera.restore();//恢复状态
		}else {
			camera.save();
			camera.translate(0, 0, centerX);		// 沿Z轴位移，看起来相当于缩小了
			camera.rotateY(rotateDegree);
			camera.translate(0, 0, -centerX);		//复位
			camera.getMatrix(matrix);
			camera.restore();
//			Log.i("llb","rotate2"+rotateDegree);
		}
		//参数是平移的距离,而不是平移目的地的坐标     确保图片的翻转过程一直处于组件的中心点位置
		matrix.preTranslate(-centerX, -centerY);//由于缩放是以(0,0)为中心的,所以为了把界面的中心与(0,0)对齐    setScale前
		matrix.postTranslate(centerX, centerY);//setScale后
	}
	
}
