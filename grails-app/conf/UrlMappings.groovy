class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?(.$format)?" {
			constraints { }
		}

		"/"(controller:'home')
		"/dashboard"(controller:'home',action:'dashboard')
		"/"(controller:'home')
		"/controllers"(view:'/index')
		"500"(view:'/error')
		"403"(view: '/login/denied')
		"404"(view:'/404')
	}
}
