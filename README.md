#extension-amazon-sns

OpenFL/NME extension for Amazon SNS (push notifications service).
This extension allows you to easily integrate AmazonSNS your OpenFL or NME game / application.

###Main Features
Amazon SNS offers push notification services through multiple platforms. This means you need to create accounts and get ID for each platform you want to use this extension.
For example: To get notifications on Android devices using Google services, you need a google cloud message enabled app and the corresponding APIs configured.

This extension supports the following service carries:
 
* GCM (google cloud message) - Done!
* Apple Push Notifications - Comming soon.
* Baidu Push Notifications (for china) - Comming soon.
* Amazon Kindle Notifications - Comming soon.

###Simple use Example
Using this extension is quite simple:
You just call init once and you'll start receiving notifications :)

To initialize the extension and begin to receive notifications.

```haxe
import extension.amazonsns.AmazonSNS;
import openfl.display.Sprite;

class Main extends Sprite {

	public function new () {
		super ();

		trace("Setting callback functions");
		AmazonSNS.onNotificationReceived = onNotificationReceived;
		AmazonSNS.onRegistrationComplete = onRegistrationComplete;
		AmazonSNS.onLoadComplete = onLoadComplete;

		trace("About to init AmazonSNS for Google Cloud Messages");
		AmazonSNS.initGCM('YOUR-GOOGLE-SENDER-ID');
	}

	public function onLoadComplete(){
		trace("Amazon SNS LOADED!");
	}

	public function onNotificationReceived(message:String){
		trace("New Notification!");
		trace(message);
	}

	public function onRegistrationComplete(registrationID:String){
		trace("Registration ID: " + registrationID);
	}
	
}
```
###Customize notifications text and icon
By default, the extension uses:
1) Your app name as title.
2) Your app icon as icon.
3) Some generic text in english as description.

You can customize notification texts (title and descriptions) by calling setNotificationTitle and setNotificationMessage. Both functions will get the singular and plural texts as parameters (for when you have one or more notifications). Automatically, the extension will replace "%%" by the number of notifications you have waiting.

Example:
```haxe
	AmazinSNS.initGCM("YOUR-SENDER-ID");
	// After calling init, you can call setNotificationTitle and setNotificationMessage;
	AmazonSNS.setNotificationTitle("New message for you...","WOW! %% new messages waiting");
	AmazonSNS.setNotificationMessage("Click here to see it!","Click here to see %% messages...");
```

To customize icons (ON ANDROID), you need to add them to the app resources so that they end up on the following folders: res/drawable-XXXX/notification-icon.png and res/drawable-XXXX/multi-notification-icon.png.
If those resources are not available for some resolution or missing at all, the extension will use the icon under res/drawable-XXXX/icon.png (wich is the openfl default location).


###How to Install

To install this library, you can simply get the library from haxelib like this:
```bash
haxelib install extension-amazon-sns
```

Once this is done, you just need to add this to your project.xml
```xml
<haxelib name="extension-amazon-sns" />
```

###Disclaimer

Amazon is a registered trademark of Amazon Technologies, Inc.
http://es.unibrander.com/estados-unidos/12554US/amazon.html

###License

The MIT License (MIT) - [LICENSE.md](LICENSE.md)

Copyright &copy; 2016 SempaiGames (http://www.sempaigames.com)

Author: Federico Bricker
