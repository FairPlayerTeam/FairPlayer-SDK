/**
 * Copyright (c) 2016-2017 FairPlayer Team
 * https://fairplayerteam.github.io/FairPlayer-SDK/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @version 1.0
 */
if (typeof Android == "undefined") {
    Android = {};
}
var globalActions = {
    objects: {
        body: $('body'),
    },
};
var customActions = {};
var registerTimeout = {
    _registry: {},
    run: function(timeoutName, timeoutFunction, timeoutDelay) {
        // Clear the timeout
        if ("undefined" !== typeof registerTimeout._registry[timeoutName]) {
            window.clearTimeout(registerTimeout._registry[timeoutName]);
        }

        // Get the timeout ID
        var id = window.setTimeout(timeoutFunction, timeoutDelay);

        // Store the timeout ID
        registerTimeout._registry[timeoutName] = id;
    },
};
$(document).ready(function() {
    var Actions = {
        _binds: function() {
            // Verify unlock feature
            do {
                try {
                    if ("function" == typeof Android["checkUnlockFeature"] && "function" == typeof Android["checkUnlockEnabled"]) {
                        // Unlock feature available
                        if (Android["checkUnlockFeature"]()) {
                            // Text based on unlocker availability
                            $('input[name=buttonUnlocker]').val(Android["checkUnlockEnabled"]() ? Android.getString("buttonUnlockerEnabled") : Android.getString("buttonUnlocker")).css({display:'inline-block'});
                            
                            // Stop here
                            break;
                        }
                    }
                } catch (exc) {}
                
                // Some error occured
                $('input[name=buttonUnlocker]').remove();
            } while (false);
            
            // Bind all actions
            $('[data-action]').click(function() {
                // Get the action name
                var action = $(this).attr('data-action');

                // Start that Android Action
                if ("function" == typeof customActions[action]) {
                    // Start the action
                    customActions[action]();
                } else {
                    // Start that Android Action
                    if ("function" == typeof Android[action]) {
                        // Start the action
                        Android[action]();
                    }
                }
            });
            
            // Tapped class functionality
            $('input[type=button]').unbind('vmousedown').on("vmousedown", function(e) {
                if (!$(this).hasClass('tapped')) {
                    $(this).addClass('tapped');
                }
            });
            $("body").unbind('vmouseup').on("vmouseup", function(e) {
                $('input[type=button]').removeClass('tapped');
            });

            // Get the strings
            $.each($('string[name], input[name]'), function() {
                // Get the string name
                var stringName = $(this).attr('name');

                // Get the new value
                var newValue = '';

                // Valid string format
                if ("undefined" != typeof stringName && stringName.length) {
                    newValue = Android.getString && Android.getString(stringName);
                }

                // Get the dynamic value for the unlocker
                if ("buttonUnlocker" == stringName) {
                    newValue = $(this).val();
                }

                // Input
                if ($(this).is("input")) {
                    if ("undefined" != typeof newValue) {
                        $(this).val(newValue);
                    }
                } else {
                    // Replace the text
                    $(this).replaceWith("undefined" != typeof newValue ? newValue : stringName);
                }
            });
        },
        _icon: function() {
            // Get the header height
            var headerIcon = $('.header .icon');
            var headerTooltip = $('.header .tooltip');

            // Resize function
            var iconResize = function() {
                if (headerIcon.outerHeight() != headerIcon.outerWidth()) {
                    headerIcon.css({
                        width: headerIcon.outerHeight() + 'px',
                        marginLeft: (-headerIcon.outerHeight() / 2) + 'px',
                    });

                    headerTooltip.css({
                        fontSize: (headerTooltip.height() / 2) + 'px',
                        lineHeight: (headerTooltip.height()) + 'px',
                    });
                }

                // Call self after 0.5 seconds
                registerTimeout.run('iconResize', iconResize, 200);
            };
            iconResize();
        },
        _buttons: function() {
            var headerTooltip = $('.header .tooltip');
            $('input[type=button]').css({
                fontSize: (headerTooltip.height() / 2) + 'px',
                lineHeight: (headerTooltip.height() / 2) + 'px',
            });
            var buttonsPadding = (($('.buttons-holder').innerHeight() - $('.buttons').innerHeight()) / 2);
            if (buttonsPadding > 0) {
                $('.buttons').css({
                    'padding-top': buttonsPadding + "px",
                });
            }
        },
        _showLoading: function() {
            var loadingGif = $('.loading .gif');
            loadingGif.css({
                width: (0.3 * $(window).width()) + 'px',
                height: (0.3 * $(window).width()) + 'px',
                marginLeft: (-0.15 * $(window).width()) + 'px',
                marginTop: (-0.15 * $(window).width()) + 'px',
            });
            var degrees = 0;
            var animateGif = function() {
                // Set the CSS
                loadingGif.css({
                    webkitTransform: "rotate(" + degrees + "deg)",
                    '-moz-transform': "rotate(" + degrees + "deg)",
                });

                // Increment
                degrees += 5;

                // Call self
                registerTimeout.run("showLoading.animateGif", function() {
                    if (degrees <= 360) {
                        animateGif();
                    } else {
                        // Show the interstitial
                        if ("function" == typeof Android['showInterstitial']) {
                            Android.showInterstitial();
                        }
                        
                        // Set all the binds
                        Actions._binds();
                        
                        // Set all the binds
                        Actions._buttons();

                        // Set the icon dimensions
                        Actions._icon();
            
                        // Stop
                        $('.loading').fadeOut(400, function(){
                            $(this).remove();
                        });

                        // Show the other elements
                        $('.header, .buttons-holder').animate({
                            opacity: 1
                        });
                    }
                }, 20);
            };
            animateGif();
        },
        run: function() {
            // Hide the loading page
            Actions._showLoading();
        },
    };

    // Run the actions
    Actions.run();
});