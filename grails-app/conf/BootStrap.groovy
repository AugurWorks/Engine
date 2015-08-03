import com.augurworks.engine.domains.Role
import com.augurworks.engine.domains.User
import com.augurworks.engine.domains.UserRole
import com.augurworks.engine.services.DataGeneratorService

class BootStrap {

	DataGeneratorService dataGeneratorService

	def createUser(name, role) {
		User me = new User(username: name).save()
		UserRole.create(me, role, true)
	}

	def init = { servletContext ->
		println 'Bootstrapping'
		def adminRole = new Role(authority: "ROLE_ADMIN").save()
		def userRole = new Role(authority: "ROLE_USER").save()
		createUser('TheConnMan', adminRole)
		createUser('safreiberg', adminRole)
		createUser('augurworks1', adminRole)

		dataGeneratorService.importQuandlDataSets()
		dataGeneratorService.generateRequest(5)
	}
}
