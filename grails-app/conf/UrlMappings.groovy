class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?(.$format)?"

		"/"(controller:'home')
		"/controllers"(view:'/index')
		"500"(view:'/error')
		"403"(view: '/login/denied')
		"404"(view:'/404')
	}
}
