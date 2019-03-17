#import "RNAlarm.h"
#import <AVFoundation/AVFoundation.h>
#import <AudioToolbox/AudioToolbox.h>

@implementation RNAlarm

- (void)setAlarm:(NSString *)triggerTime andStatus:(NSString *)status {
  NSUserDefaults *userDefault = [NSUserDefaults standardUserDefaults];
  [userDefault setObject:status forKey:triggerTime];
  [userDefault synchronize];
}

- (int)getAlarmStatus:(NSString *)triggerTime {
  NSUserDefaults *userDefault = [NSUserDefaults standardUserDefaults];
  NSString *strStatus = [userDefault objectForKey:triggerTime];

  if (strStatus == nil) {
    return -1;
  } else {
    return [strStatus isEqualToString:@"error"] ? NO : YES;
  }
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center
       willPresentNotification:(UNNotification *)notification
         withCompletionHandler:
(void (^)(UNNotificationPresentationOptions))completionHandler {

  completionHandler(UNNotificationPresentationOptionAlert |
                    UNNotificationPresentationOptionSound);

  AudioServicesPlayAlertSoundWithCompletion(kSystemSoundID_Vibrate, nil);
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center
didReceiveNotificationResponse:(UNNotificationResponse *)response
         withCompletionHandler:(void (^)())completionHandler {

  NSString *categoryIdentifier =
  response.notification.request.content.categoryIdentifier;
  NSString *identifier1 = [categoryIdentifier stringByAppendingString:@"1"];
  NSString *identifier2 = [categoryIdentifier stringByAppendingString:@"2"];
  NSString *identifier3 = [categoryIdentifier stringByAppendingString:@"3"];

  [center removePendingNotificationRequestsWithIdentifiers:@[
                                                             identifier1, identifier2, identifier3
                                                             ]];
  [center removeDeliveredNotificationsWithIdentifiers:@[
                                                        identifier1, identifier2, identifier3
                                                        ]];

  completionHandler();
}

- (dispatch_queue_t)methodQueue {
  return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(clearAlarm) {
  NSUserDefaults *userDefault = [NSUserDefaults standardUserDefaults];
  NSDictionary *dic = [userDefault dictionaryRepresentation];
  for (id key in dic) {
    [userDefault removeObjectForKey:key];
  }
  [userDefault synchronize];
  [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];
  [[UIApplication sharedApplication] cancelAllLocalNotifications];
}

RCT_EXPORT_METHOD(initAlarm:successCallback
                  : (RCTResponseSenderBlock)callback) {

  UNUserNotificationCenter *rCenter =
  UNUserNotificationCenter.currentNotificationCenter;
  [rCenter
   requestAuthorizationWithOptions:(UNAuthorizationOptionAlert |
                                    UNAuthorizationOptionSound)
   completionHandler:^(BOOL granted,
                       NSError *_Nullable error) {
     // Enable or disable based on authorization
     if (granted == YES) {
       NSArray *result = [NSArray
                          arrayWithObjects:[NSNumber numberWithBool:NO],
                          nil];
       if (successCallback != nil) {
         result = [NSArray
                   arrayWithObjects:[NSNumber
                                     numberWithBool:YES],
                   nil];
       }
       callback(result);
     }
   }];
}

RCT_EXPORT_METHOD(setAlarm
                  : (NSString *)triggerTime title
                  : (NSString *)title musicUri
                  : (NSString *)musicUri successCallback
                  : (RCTResponseSenderBlock)successCallback errorCallback
                  : (RCTResponseSenderBlock)errorCallback) {
  @try {
    int alarmStatus = [self getAlarmStatus:triggerTime];

    bool isSettedAlarm = [NSNumber numberWithInt:alarmStatus].boolValue;
    if (alarmStatus != -1) {
      if (isSettedAlarm) {
        NSArray *result = [NSArray arrayWithObjects:@"0", nil];
        if (successCallback != nil) {
          successCallback(result);
          return;
        }
      } else {
        NSArray *result = [NSArray arrayWithObjects:@"0", nil];
        if (errorCallback != nil) {
          errorCallback(result);
          return;
        }
      }
    }

    UNUserNotificationCenter *rCenter =
    UNUserNotificationCenter.currentNotificationCenter;
    [rCenter requestAuthorizationWithOptions:(UNAuthorizationOptionAlert |
                                              UNAuthorizationOptionSound)
                           completionHandler:^(BOOL granted,
                                               NSError *_Nullable error){
                             // Enable or disable based on authorization
                           }];
    UNMutableNotificationContent *content =
    [[UNMutableNotificationContent alloc] init];
    content.body = [NSString localizedUserNotificationStringForKey:title
                                                         arguments:nil];
    content.categoryIdentifier = triggerTime;

    if (musicUri == nil) {
      content.sound = [UNNotificationSound defaultSound];
    } else {
      musicUri = [musicUri stringByAppendingString:@".mp3"];
      content.sound = [UNNotificationSound soundNamed:musicUri];
    }

    NSDate *date = [NSDate date];
    double nowSeconds = [date timeIntervalSince1970];

    double startDate = [triggerTime doubleValue];

    double intervalSeconds = startDate / 1000 - nowSeconds;
    if (intervalSeconds > 0) {

      UNTimeIntervalNotificationTrigger *trigger =
      [UNTimeIntervalNotificationTrigger
       triggerWithTimeInterval:intervalSeconds
       repeats:NO];

      UNNotificationRequest *request = [UNNotificationRequest
                                        requestWithIdentifier:[triggerTime stringByAppendingString:@"1"]
                                        content:content
                                        trigger:trigger];

      UNUserNotificationCenter *center =
      [UNUserNotificationCenter currentNotificationCenter];

      UNNotificationAction *action = [UNNotificationAction
                                      actionWithIdentifier:@"clear.repeat.action"
                                      title:@"Timer has elapsed."
                                      options:UNNotificationActionOptionForeground];
      UNNotificationCategory *category = [UNNotificationCategory
                                          // categoryWithIdentifier:@"RNAlarmCategory"
                                          categoryWithIdentifier:triggerTime
                                          actions:@[ action ]
                                          intentIdentifiers:@[]
                                          options:
                                          UNNotificationCategoryOptionCustomDismissAction];

      [center
       setNotificationCategories:[NSSet setWithObjects:category, nil]];
      [center addNotificationRequest:request
               withCompletionHandler:^(NSError *_Nullable error) {
                 if (error != nil) {
                   @throw error;
                 }
               }];

      center.delegate = self;
      [self setAlarm:triggerTime andStatus:@"success"];

      NSArray *result = [NSArray arrayWithObjects:@"0", nil];
      if (successCallback != nil) {
        successCallback(result);
      }
    } else {
      [self setAlarm:triggerTime andStatus:@"error"];

      NSArray *result = [NSArray arrayWithObjects:@"0", nil];
      if (errorCallback != nil) {
        errorCallback(result);
      }
    }

  } @catch (NSException *exception) {
    NSLog(@"%@", exception.reason);
    NSArray *result =
    [NSArray arrayWithObjects:@"1", exception.reason, nil];
    if (errorCallback != nil) {
      errorCallback(result);
    }
  }
}

@end
