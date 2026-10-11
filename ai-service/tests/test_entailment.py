import unittest
from ai_service.services.entailment import EntailmentService
from ai_service.providers.base import ProviderError
from tests.test_answer import ReplyProvider


class EntailmentTest(unittest.TestCase):
    def request(self):
        return {'claims':[{'id':0,'text':'You may omit a lift in a 15 m building.',
                'evidence':['Provision for lifts shall be made for building more than 12.5 meters in height.']}],
                'calculation':{},'question':'Do I need a lift?'}

    def test_preserves_contradiction_and_requires_independent_evidence_review(self):
        provider=ReplyProvider({'verdicts':[{'id':0,'verdict':'contradicted','reason':'Reverses the lift obligation.'}]})
        result=EntailmentService(provider).verify(self.request())
        self.assertEqual('contradicted',result['verdicts'][0]['verdict'])
        self.assertIn('Provision for lifts',provider.last_user_message)

    def test_incomplete_duplicate_or_unknown_verdict_fails_closed(self):
        for verdicts in [[],[{'id':1,'verdict':'entailed'}],[{'id':0,'verdict':'maybe'}]]:
            with self.subTest(verdicts=verdicts),self.assertRaises(ProviderError):
                EntailmentService(ReplyProvider({'verdicts':verdicts})).verify(self.request())

    def test_missing_evidence_and_unavailable_provider_never_approve(self):
        request=self.request();request['claims'][0]['evidence']=[]
        with self.assertRaises(ValueError):
            EntailmentService(ReplyProvider({})).verify(request)
        provider=ReplyProvider({});provider.configured=False
        with self.assertRaises(ProviderError):
            EntailmentService(provider).verify(self.request())
