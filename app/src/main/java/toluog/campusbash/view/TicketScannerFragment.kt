package toluog.campusbash.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import toluog.campusbash.R


private const val EVENT_ID_PARAM = "eventId"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TicketScannerFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TicketScannerFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class TicketScannerFragment : Fragment() {
    private var eventId: String = ""
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getString(EVENT_ID_PARAM) ?: ""
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ticket_scanner, container, false)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param eventId Parameter 1.
         * @return A new instance of fragment TicketScannerFragment.
         */
        @JvmStatic
        fun newInstance(eventId: String) =
                TicketScannerFragment().apply {
                    arguments = Bundle().apply {
                        putString(EVENT_ID_PARAM, eventId)
                    }
                }
    }

}
