package extension.amazonsns;

import openfl.Lib;

class AmazonSNS {
	private static var instance:AmazonSNS = null;
	private static var initialized:Bool = false;

	public static function initGCM(senderID:String){
		#if android
		if(initialized) return;
		initialized = true;
		try{
			instance = new AmazonSNS();

			// JNI METHOD LINKING
			var __init:String->AmazonSNS->Void = openfl.utils.JNI.createStaticMethod("org/haxe/extension/amazonsns/AmazonSNS", "init", "(Ljava/lang/String;Lorg/haxe/lime/HaxeObject;)V");
			__init(senderID, instance);
		}catch(e:Dynamic){
			trace("Android INIT Exception: "+e);
		}
		#end
	}

	private function new(){}

	public function onMessage(s:String){
		trace("HAY MENSAJE: "+s);
	}
}
