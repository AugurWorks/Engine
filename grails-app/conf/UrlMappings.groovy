class UrlMappings {

	static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
		
		"/"(controller:'home')
        "/controllers"(view:'/index')
        "500"(view:'/error')
        "403"(view: '/login/denied')
		"404"(view:'/404')
	}
}
