export default class Workflow {
  static STEPS = {
    entry: 'ENTRY',
    cleansing: 'CLEANSING',
    analysis: 'ANALYSIS',
  }

  static STEP_CODES = Object.values(Workflow.STEPS)

  static NEXT_STEP_BY_STEP = {
    [Workflow.STEPS.entry]: Workflow.STEPS.cleansing,
    [Workflow.STEPS.cleansing]: Workflow.STEPS.analysis,
    [Workflow.STEPS.analysis]: null,
  }

  static PREV_STEP_BY_STEP = {
    [Workflow.STEPS.entry]: null,
    [Workflow.STEPS.cleansing]: Workflow.STEPS.entry,
    [Workflow.STEPS.analysis]: Workflow.STEPS.cleansing,
  }

  static getNextStep(step) {
    return Workflow.NEXT_STEP_BY_STEP[step]
  }

  static getPrevStep(step) {
    return Workflow.PREV_STEP_BY_STEP[step]
  }
}
