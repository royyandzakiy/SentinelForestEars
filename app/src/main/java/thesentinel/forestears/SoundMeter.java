/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package thesentinel.forestears;


import android.media.MediaRecorder;
import android.util.Log;

public class SoundMeter {
	static final private double EMA_FILTER = 0.6;

	private MediaRecorder mRecorder = null;
	private double mEMA = 0.0;

	public void start() {
		Log.d("DEBUG","SoundMeter::start()");
		if (mRecorder == null) {
			try {
				mRecorder = new MediaRecorder();
				mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				mRecorder.setOutputFile("/dev/null");
				mRecorder.prepare();
		    	mRecorder.start();
			} catch (Exception e) {
				Log.e("ERROR",e.getMessage());
				e.printStackTrace();
			}
		    mEMA = 0.0;
		}
	}
	
	public void stop() {
		Log.d("DEBUG","SoundMeter::stop()");
		if (mRecorder != null) {
			mRecorder.stop();	
			mRecorder.release();
			mRecorder = null;
		}
	}
	
	public double getAmplitude() {
		if (mRecorder != null)
			return  (mRecorder.getMaxAmplitude()/2700.0);
		else
			return 0;

	}

	public double getAmplitudeEMA() {
		double amp = getAmplitude();
		mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
		return mEMA;
	}
}