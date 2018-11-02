export default class Workflow {

    static STEPS = {
        entry: {
            code: 'ENTRY',
            label: 'Entry'
        },
        cleansing: {
            code: 'CLEANSING',
            label: 'Cleansing'
        },
        analysis: {
            code: 'ANALYSIS',
            label: 'Analysis'
        }
    }

    static STEP_CODES = Object.keys(Workflow.STEPS).map(s => Workflow.STEPS[s].code)
}