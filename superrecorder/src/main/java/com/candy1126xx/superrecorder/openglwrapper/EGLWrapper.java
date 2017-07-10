package com.candy1126xx.superrecorder.openglwrapper;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * Created by Administrator on 2017/6/20 0020.
 */

public class EGLWrapper {

    private EGL10 _egl = (EGL10) EGLContext.getEGL();
    private EGLDisplay mEGLDisplay;
    private EGLContext mEGLContext;
    private EGLConfig mEGLConfig;
    private String mEglExtensions;

    public EGLWrapper(EGLContext sharedContext, int flags) {
        this.mEGLDisplay = EGL11.EGL_NO_DISPLAY;
        this.mEGLContext = EGL11.EGL_NO_CONTEXT;
        this.mEGLConfig = null;
        if(sharedContext == null) {
            sharedContext = EGL11.EGL_NO_CONTEXT;
        }

        this.mEGLDisplay = this._egl.eglGetDisplay(EGL11.EGL_DEFAULT_DISPLAY);
        if(this.mEGLDisplay == EGL11.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        } else if(!this._egl.eglInitialize(this.mEGLDisplay, (int[])null)) {
            this.mEGLDisplay = null;
            throw new RuntimeException("unable to initialize EGL14");
        } else {
            this.mEglExtensions = this._egl.eglQueryString(this.mEGLDisplay, EGL11.EGL_EXTENSIONS);
            EGLConfig config = this.getConfig(flags);
            if(config == null) {
                throw new RuntimeException("Unable to find a suitable EGLConfig");
            } else {
                short EGL_CONTEXT_CLIENT_VERSION = 0x3098;
                int[] attrib2_list = new int[]{EGL_CONTEXT_CLIENT_VERSION, 2, EGL11.EGL_NONE};
                EGLContext context = this._egl.eglCreateContext(this.mEGLDisplay, config, sharedContext, attrib2_list);
                this.checkEglError("eglCreateContext");
                this.mEGLConfig = config;
                this.mEGLContext = context;
            }
        }
    }

    private EGLConfig getConfig(int flags) {
        int[] attribList = new int[]{
                EGL11.EGL_RED_SIZE, 8,
                EGL11.EGL_GREEN_SIZE, 8,
                EGL11.EGL_BLUE_SIZE, 8,
                EGL11.EGL_ALPHA_SIZE, 8,
                EGL11.EGL_RENDERABLE_TYPE, 4,
                EGL11.EGL_SURFACE_TYPE, 5,
                EGL11.EGL_NONE, 0,
                EGL11.EGL_NONE};
        if((flags & 1) != 0 && this.mEglExtensions != null) {
            if(this.mEglExtensions.contains("EGL_ANDROID_recordable") && Build.MODEL.equals("M351") && Build.VERSION.SDK_INT != 19) {
                attribList[attribList.length - 3] = 0x3142;
                attribList[attribList.length - 2] = 0x0001;
            }
        }

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if(!this._egl.eglChooseConfig(this.mEGLDisplay, attribList, configs, configs.length, numConfigs)) {
            return null;
        } else {
            return configs[0];
        }
    }

    public void release() {
        if(this.mEGLContext != EGL11.EGL_NO_CONTEXT) {
            this._egl.eglMakeCurrent(this.mEGLDisplay, EGL11.EGL_NO_SURFACE, EGL11.EGL_NO_SURFACE, EGL11.EGL_NO_CONTEXT);
            this._egl.eglDestroyContext(this.mEGLDisplay, this.mEGLContext);
            this._egl.eglTerminate(this.mEGLDisplay);
        }

        this.mEGLDisplay = EGL11.EGL_NO_DISPLAY;
        this.mEGLContext = EGL11.EGL_NO_CONTEXT;
        this.mEGLConfig = null;
    }

    public void releaseSurface(EGLSurface eglSurface) {
        this._egl.eglDestroySurface(this.mEGLDisplay, eglSurface);
    }

    public EGLSurface createWindowSurface(Object surface) {
        if(!(surface instanceof Surface) && !(surface instanceof SurfaceTexture) && !(surface instanceof SurfaceHolder)) {
            throw new RuntimeException("invalid surface: " + surface);
        } else {
            int[] surfaceAttributes = new int[]{EGL11.EGL_NONE};
            EGLSurface eglSurface = this._egl.eglCreateWindowSurface(this.mEGLDisplay, this.mEGLConfig, surface, surfaceAttributes);
            this.checkEglError("eglCreateWindowSurface");
            if(eglSurface == null) {
                throw new RuntimeException("surface was null");
            } else {
                return eglSurface;
            }
        }
    }

    public EGLSurface createPbufferSurface(int w, int h) {
        int[] surfaceAttributes = new int[]{EGL11.EGL_WIDTH, w, EGL11.EGL_HEIGHT, h, EGL11.EGL_NONE};
        EGLSurface eglSurface = this._egl.eglCreatePbufferSurface(this.mEGLDisplay, this.mEGLConfig, surfaceAttributes);
        if(eglSurface == EGL10.EGL_NO_SURFACE) {
            this.checkEglError("createPbufferSurface");
            throw new RuntimeException("surface was null");
        } else {
            return eglSurface;
        }
    }

    public void makeCurrent(EGLSurface eglSurface) {
        if(!this._egl.eglMakeCurrent(this.mEGLDisplay, eglSurface, eglSurface, this.mEGLContext)) {
            this.checkEglError("Make current");
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    public boolean swapBuffers(EGLSurface eglSurface) {
        return this._egl.eglSwapBuffers(this.mEGLDisplay, eglSurface);
    }

    public void setPresentationTime(long nsecs) {
        if(this.mEglExtensions != null && this.mEglExtensions.contains("EGL_ANDROID_presentation_time")) {
            android.opengl.EGLDisplay display = EGL14.eglGetCurrentDisplay();
            android.opengl.EGLSurface surface = EGL14.eglGetCurrentSurface(12377);
            EGLExt.eglPresentationTimeANDROID(display, surface, nsecs);
        }

    }

    private void checkEglError(String msg) {
        int error;
        if((error = this._egl.eglGetError()) != 12288) {
            throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }

}
