        /**
         * Function: addMarker
         * Add a new marker to the markers layer given the following lonlat, 
         *     popupClass, and popup contents HTML. Also allow specifying 
         *     whether or not to give the popup a close box.
         * 
         * Parameters:
         * ll - {<OpenLayers.LonLat>} Where to place the marker
         * popupClass - {<OpenLayers.Class>} Which class of popup to bring up 
         *     when the marker is clicked.
         * popupContentHTML - {String} What to put in the popup
         * closeBox - {Boolean} Should popup have a close box?
         * overflow - {Boolean} Let the popup overflow scrollbars?
         */
        function addMarker(ll, popupClass, popupContentHTML, closeBox, overflow) {

            var feature = new OpenLayers.Feature(markers, ll); 
            feature.closeBox = closeBox;
            feature.popupClass = popupClass;
            feature.data.popupContentHTML = popupContentHTML;
            feature.data.overflow = (overflow) ? "auto" : "hidden";
                    
            var marker = feature.createMarker();

            var markerClick = function (evt) {
                if (this.popup == null) {
                    this.popup = this.createPopup(this.closeBox);
                    map.addPopup(this.popup);
                    this.popup.show();
                } else {
                    this.popup.toggle();
                }
                currentPopup = this.popup;
                OpenLayers.Event.stop(evt);
            };
            marker.events.register("mousedown", feature, markerClick);

            markers.addMarker(marker);
        }
