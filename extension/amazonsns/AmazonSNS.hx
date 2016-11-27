package extension.amazonsns;

import openfl.Lib;
import haxe.Timer;

class AmazonSNS {
	private static var instance:AmazonSNS = null;
	private static var initialized:Bool = false;

	public static var onNotificationsReceived:String->Void = null;
	public static var onRegistrationSuccess:String->Void = null;
	public static var onRegistrationError:String->Void = null;

	public static var setNotificationTitle(default,null):String->String->Bool = function(single:String,multiple:String) return true;
	public static var setNotificationMessage(default,null):String->String->Bool = function(single:String,multiple:String) return true;
	public static var getRegistrationId(default,null):Void->String = function() return "";
	public static var getRegistrationError(default,null):Void->String = function() return "";
	public static var registerRetry(default,null):Void->Void = function() return;

	public static function initGCM(senderID:String){
		#if android
		if(initialized) return;
		initialized = true;
		try{
			instance = new AmazonSNS();
			// JNI METHOD LINKING
			var __init:String->AmazonSNS->Void = openfl.utils.JNI.createStaticMethod("org/haxe/extension/amazonsns/AmazonSNS", "init", "(Ljava/lang/String;Lorg/haxe/lime/HaxeObject;)V");
			setNotificationTitle = openfl.utils.JNI.createStaticMethod("org/haxe/extension/amazonsns/AmazonSNS", "setNotificationTitles", "(Ljava/lang/String;Ljava/lang/String;)Z");
			setNotificationMessage = openfl.utils.JNI.createStaticMethod("org/haxe/extension/amazonsns/AmazonSNS", "setNotificationMessages", "(Ljava/lang/String;Ljava/lang/String;)Z");
			getRegistrationId = openfl.utils.JNI.createStaticMethod("org/haxe/extension/amazonsns/AmazonSNS", "getRegistrationId", "()Ljava/lang/String;");
			getRegistrationError = openfl.utils.JNI.createStaticMethod("org/haxe/extension/amazonsns/AmazonSNS", "getRegistrationError", "()Ljava/lang/String;");
			registerRetry = openfl.utils.JNI.createStaticMethod("org/haxe/extension/amazonsns/AmazonSNS", "registerRetry", "()V");
			__init(senderID, instance);
		}catch(e:Dynamic){
			trace("Android INIT Exception: "+e);
		}
		#end
	}


	private function new(){}

	public function _onNotificationsReceived(json:String){
		if(onNotificationsReceived!=null) {
			Timer.delay(function(){ onNotificationsReceived(json); }, 0);
		} else {
			trace("Notification received: "+json);
		}		
	}

	public function _onRegistrationSuccess(registrationID:String) {
		if(onRegistrationSuccess!=null) {
			Timer.delay(function(){ onRegistrationSuccess(registrationID); }, 0);
		} else {
			trace("Registration success: "+registrationID);
		}
	}

	public function _onRegistrationError(errorMessage:String) {
		if(onRegistrationError!=null) {
			Timer.delay(function(){ onRegistrationError(errorMessage); }, 0);
		} else {
			trace("Registration error: "+errorMessage);
		}
	}
}
