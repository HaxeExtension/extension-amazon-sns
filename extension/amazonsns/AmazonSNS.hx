package extension.amazonsns;

import openfl.Lib;

class AmazonSNS {
	private static var instance:AmazonSNS = null;
	private static var initialized:Bool = false;

	public static var onNotificationReceived:String->Void = null;
	public static var onRegistrationComplete:String->Void = null;

	public static var setNotificationTitle(default,null):String->String->Bool = null;
	public static var setNotificationMessage(default,null):String->String->Bool = null;

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
			__init(senderID, instance);
		}catch(e:Dynamic){
			trace("Android INIT Exception: "+e);
		}
		#end
	}


	private function new(){}

	public function _onMessage(s:String){
		if(onNotificationReceived!=null) {
			onNotificationReceived(s);
		} else {
			trace("AmazonSNS: unhandled notification received!");
			trace(s);
		}
	}
}
