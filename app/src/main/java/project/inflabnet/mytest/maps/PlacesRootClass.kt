package android.inflabnet.infsocial.maps

/**
 * Root elemento for Gson.
 * The json result of Goolgle Map Search has the following format>
 * {
 *      "error_message" : "",
 *      "html_attributions" : [],
 *      "results" : [],
 *      "status" : "OK"
 * }
 * @see [https://developers.google.com/places/web-service/search?hl=pt-br]
 */
data class PlacesRootClass(val error_message: String?,
                           val html_attributions: List<String>,
                           val results: List<Result>,
                           val status: String)

/**
 * Container for the result objects for Gson.
 *          {
 *              "geometry" : {},
 *              "icon" : "http://maps.gstatic.com/mapfiles/place_api/icons/travel_agent-71.png",
 *              "id" : "21a0b251c9b8392186142c798263e289fe45b4aa",
 *              "name" : "Rhythmboat Cruises",
 *              "opening_hours" : { },
 *              "photos" : [],
 *              "place_id" : "ChIJyWEHuEmuEmsRm9hTkapTCrk",
 *              "scope" : "GOOGLE",
 *              "alt_ids" : [],
 *              "reference" : "CoQBdQAAAFSiijw5-cAV68xdf2O18pKIZ0seJh03u9h9wk_lEdG-cP1dWvp_QGS4SNCBMk_fB06YRsfMrNkINtPez22p5lRIlj5ty_HmcNwcl6GZXbD2RdXsVfLYlQwnZQcnu7ihkjZp_2gk1-fWXql3GQ8-1BEGwgCxG-eaSnIJIBPuIpihEhAY1WYdxPvOWsPnb2-nGb6QGhTipN0lgaLpQTnkcMeAIEvCsSa0Ww",
 *              "types" : [ "travel_agency", "restaurant", "food", "establishment" ],
 *              "vicinity" : "Pyrmont Bay Wharf Darling Dr, Sydney"
 *          }
 */
data class Result(val geometry: QuerySearchGeometryInfo,
                  val icon: String,
                  val id: String,
                  val name: String,
                  val opening_hours: QuerySearchOpeningInfo,
                  val photos: List<QuerySearchPhotosInfo>,
                  val place_id: String,
                  val scope: String,
                  val alt_ids: List<QuerySearchAlteranteIdInfo>,
                  val reference: String,
                  val types: List<String>,
                  val vicinity: String)

/**
 * Container for the result geometry for Gson.
 *              {
 *                  "location" : { }
 *              }
 */
data class QuerySearchGeometryInfo(val location: QuerySearchLocationInfo)

/**
 * Container for the result location for Gson.
 *                  {
 *                      "lat" : -33.870775,
 *                      "lng" : 151.199025
 *                  }
 */
data class QuerySearchLocationInfo(val lat: Double, val lng: Double)

/**
 * Container for the opening_hours objects for Gson.
 *              {
 *                  "open_now" : true
 *              }
 */
data class QuerySearchOpeningInfo(val open_now: Boolean)

/**
 * Container for the photos objects for Gson.
 *                  {
 *                      "height" : 270,
 *                      "html_attributions" : [],
 *                      "photo_reference" : "CnRnAAAAF-LjFR1ZV93eawe1cU_3QNMCNmaGkowY7CnOf-kcNmPhNnPEG9W979jOuJJ1sGr75rhD5hqKzjD8vbMbSsRnq_Ni3ZIGfY6hKWmsOf3qHKJInkm4h55lzvLAXJVc-Rr4kI9O1tmIblblUpg2oqoq8RIQRMQJhFsTr5s9haxQ07EQHxoUO0ICubVFGYfJiMUPor1GnIWb5i8",
 *                      "width" : 519
 *                  }
 */
data class QuerySearchPhotosInfo(val height: Int,
                                 val html_attributions: List<String>,
                                 val photo_reference: String,
                                 val width: Int)

/**
 * Container for the alt_ids objects for Gson.
 *                  {
 *                      "place_id" : "D9iJyWEHuEmuEmsRm9hTkapTCrk",
 *                      "scope" : "APP"
 *                  }
 */
data class QuerySearchAlteranteIdInfo(val place_id: String, val scope: String)