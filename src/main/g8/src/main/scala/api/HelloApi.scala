package api

class HelloApi(val executor: ExecutionContext)
    extends ApiStack {
      
  before() {
    contentType = "application/json"
    response.headers += ("Access-Control-Allow-Origin" -> "*")
  }
  
  get("/") {
    """{"response": "Hello World"}"""
  }
  
  get("/name/:name") {
    val name = params.get("name").getOrElse(halt(BadRequest(
          body = "{}",
          reason = "A 'name' parameter is required"
        )))
    s"""{"response": "Hello $name"}"""
  }
}