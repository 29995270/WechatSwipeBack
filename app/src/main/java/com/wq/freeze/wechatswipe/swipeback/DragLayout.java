package com.wq.freeze.wechatswipe.swipeback;

/**
 * Created by wangqi on 2016/8/4.
 */
public interface DragLayout {

    void addDragCallback(DragCallback callback);

    void setFinishCallback(FinishCallback callback);

    abstract class DragCallback {
        public abstract void onArrived();
        public void onStart(){}
        public void onProcessing(float percent){}
        public void onRelease(boolean notEnough){}
        public void onReturned(){}
    }

    interface FinishCallback {
        void onFinish();
    }
}
