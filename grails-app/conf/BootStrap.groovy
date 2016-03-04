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
		if (Role.count() == 0) {
			Role adminRole = new Role(authority: "ROLE_ADMIN").save()
			Role userRole = new Role(authority: "ROLE_USER").save()
			createUser('TheConnMan', adminRole)
			createUser('safreiberg', adminRole)
			createUser('augurworks1', adminRole)
			createUser('gbcolema11', adminRole)

			dataGeneratorService.importQuandlDataSets()
			dataGeneratorService.importLocalDataSets()
			dataGeneratorService.bootstrapDefaultRequests()
		}
	}
}
