import com.sap.piper.ConfigurationHelper
import com.sap.piper.GitUtils
import groovy.text.GStringTemplateEngine
import groovy.transform.Field

import static com.sap.piper.Prerequisites.checkScript

@Field String STEP_NAME = getClass().getName()
@Field String METADATA_FILE = 'metadata/uiVeri5ExecuteTests.yaml'

@Field Set CONFIG_KEYS = [
    "testRepository",
]

void call(Map parameters = [:]) {
    final script = checkScript(this, parameters) ?: this
    String stageName = parameters.stageName ?: env.STAGE_NAME
    Map config = ConfigurationHelper.newInstance(this)
            .loadStepDefaults([:], stageName)
            .mixinGeneralConfig(script.commonPipelineEnvironment, CONFIG_KEYS)
            .mixinStepConfig(script.commonPipelineEnvironment, CONFIG_KEYS)
            .mixinStageConfig(script.commonPipelineEnvironment, stageName, CONFIG_KEYS)
            .mixin(parameters, CONFIG_KEYS)
            .use()


    parameters.config.stashContent = config.testRepository ? [GitUtils.handleTestRepository(this, parameters.config)] : utils.unstashAll(parameters.config.stashContent)

    List credentials = [
        [type: 'usernamePassword', id: 'seleniumHubCredentialsId', env: ['PIPER_SELENIUM_HUB_USER', 'PIPER_SELENIUM_HUB_PASSWORD']],
    ]
    piperExecuteBin(parameters, STEP_NAME, METADATA_FILE, credentials)
}
