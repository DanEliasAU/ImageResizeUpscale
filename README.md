# ImageResizeUpscale
First Android project - Porting a simple web-based app to Android using Kotlin &amp; Jetpack Compose

This is my first 'from scratch' Android project after working through several Google tutorials on the basics of Android app development. It is a port of another project of mine that is a web-based application that allows one to upload an image (locally or via URL) and resize/compress it via sliders and then save it as a JPEG image. I later integrated it with Waifu2x via an API endpoint on a local network machine to allow upscaling.


Known issues to fix:
- Catch and handling of exceptions relating to fetching images via URL
- Catch and handling of exceptions relating to upscaling images
- <strike>Error checking of the input file name</strike>
- <strike>Keyboard not hiding in certain circumstances (click 'Done' keyboard button, click 'Go' on start screen)</strike>
- Slider not accurately representing finger position
    -> May need to use a different slider/try implementing my own
- <strike>The start screen stays interactable while calling network functions
    -> Either add loading activity between start screen and options screen or move straight to options screen and use placeholders/disallow interaction</strike>
- Loading screen not shown on pressing 'Go' (URL image) on start screen

Future Implementations:
- <strike>Add loading/progress animations</strike>
- Custom logos/icons
    -> Learning a bit of svg for this
- Alignment, spacing and styling of compositions (particularly for different sized devices)
- Allow user to specify which app to open an image from
