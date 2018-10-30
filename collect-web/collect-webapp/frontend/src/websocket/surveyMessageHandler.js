const messageTypePrefix = 'SURVEY_'

const surveyTypes = {
    created: messageTypePrefix + 'CREATED',
    updated: messageTypePrefix + 'UPDATED',
    published: messageTypePrefix + 'PUBLISHED',
    unpublished: messageTypePrefix + 'UNPUBLISHED',
    deleted: messageTypePrefix + 'DELETED'
}

const handleSurveyMessage = (props, message) => {
    const {surveyCreated, surveyUpdated, surveyDeleted} = props
    const {type, survey} = message

    switch(type) {
        case surveyTypes.created:
            surveyCreated(survey)
            break
        case surveyTypes.updated:
        case surveyTypes.published:
        case surveyTypes.unpublished:
            surveyUpdated(survey)
            break
        case surveyTypes.deleted:
            surveyDeleted(survey)
            break
        default:
    }
}

export default handleSurveyMessage