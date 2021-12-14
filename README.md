# CurrrencyConverter
Android app interfacing with currencyLayer API to get exchange rates for various currencies.

# Mobile Developer Challenge Submission

The app was developed in Kotlin (Android) and covers al the tasks I set out to deliver on.

### Notes

1. API properties (keys, endpoint, base urls) are defined in the file apikey.properties. The file also contains all necessary attributes for the running of the app

2. The file - keystore.properties holds the information for building a release build for the app. You may need to adjust these to your settings when necessary

3. The app has an integration to Firebase (Crashlytics, Analytics, Events)

4. currencyLayer API limits the **endpoint(live)** to one currency (USD)

5. A private GIT repo is available. Accesss will be granted on request.

### Tests
The tests are in the respective test folders. 

I have written a test case for the Conversions.

However, there seems to be an issue with the WorkfManagerTest and ConvertDBTest files. 
This is related to the gradle plugin not pulling in properly.

### Features
1. WorkManager for Managing the scheduled retrieval on rates
2. RoomDB for persisting the currencyLayer API information
3. ViewModel for accessing the Room DB objects
4. DiffUtils for RecyclerView updates

### Mode of Use
1. Run the app to a device or emulator
2. The list of currencies will be loaded into the CURRENCY box.
3. The deault currency rate is 1 (1:1) 
4. Select any currency and the app will refrech the list to show the rates for the selected currency
5. The app will also attempt to retrieve the rate from the API when 4 is selected and will use the room db observe pattern to refresh the list
