package engine

class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?(.$format)?" {
			constraints { }
		}

		"/rss/$apiKey/$productName"(controller: 'rss')

		"/"(controller:'home')
		"/dashboard"(controller:'home',action:'dashboard')
		"/controllers"(view:'/index')
		"500"(view:'/error')
		"403"(view: '/login/denied')
		"404"(view:'/404')
		"/tag/show/$tag"(controller:'tag', action:'show')
	}
}
